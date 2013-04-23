(ns empirics.workflow
  (:use [cascalog.api]
        [cascalog.checkpoint :only (workflow)]
        [empirics.grabdata]
        [empirics.edge]
        [empirics.cluster]
        [empirics.margin])
  (:require [cascalog.ops :as ops]))

(def production-map
  "Bound to a map that contains the relevant S3 file paths to process
  the raw forma data into a form that can be analyzed locally."
  {:raw-path      "s3n://pailbucket/all-prob-series"
   :static-path   "s3n://pailbucket/all-static-seq/all"
   :edge-path     "s3n://formatemp/empirical-paper/edges"
   :cluster-path  "s3n://formatemp/empirical-paper/cluster"})

(defmain FirstStage
  "Accepts a global static pixel-characteristic and dynamic
  probability source from `seqfile-map`.  Returns the edges between
  deforested pixels in Borneo."
  [tmp-root & {:keys [probability-thresh
                      path-map]
               :or   {probability-thresh 50
                      path-map production-map}}]
  (?- (hfs-seqfile screen-path :sinkmode :replace)
      (borneo-hits (-> :raw-path path-map hfs-seqfile)
                   (-> :static-path path-map hfs-seqfile)
                   probability-thresh)))

;; (defmain FirstStage
;;   "Accepts a global static pixel-characteristic and dynamic
;;   probability source from `seqfile-map`.  Returns the edges between
;;   deforested pixels in Borneo."
;;   [tmp-root & {:keys [distance-thresh
;;                       probability-thresh
;;                       path-map]
;;                :or   {distance-thresh 0.01
;;                       probability-thresh 50
;;                       path-map production-map}}]

;;   (workflow [tmp-root]

;;             ;; Extract only pixels that were cleared in Borneo
;;             screen-step
;;             ([:tmp-dirs screen-path]
;;                (?- (hfs-seqfile screen-path :sinkmode :replace)
;;                    (borneo-hits (-> :raw-path path-map hfs-seqfile)
;;                                 (-> :static-path path-map hfs-seqfile)
;;                                 probability-thresh)))

;;             ;; Sink edges for nearby pixels
;;             edge-step
;;             ([]
;;                (?- (-> :edge-path path-map (hfs-seqfile :sinkmode :replace))
;;                    (create-edges (hfs-seqfile screen-path) distance-thresh)))))

(defmain SecondStage
  "Sink the cluster identifiers for each pixel.  Accepts a source with
  the edges between pixels, and sinks the clusters (a result of a
  strongly connected graph algorithm) into a sequence file."
  [& {:keys [path-map] :or {path-map production-map}}]
  (let [edge-src-path (-> :edge-path path-map hfs-seqfile)
        graph (-> edge-src-path hfs-seqfile make-graph)
        src (cluster-src graph)]
    (?<- (-> :cluster-path path-map (hfs-seqfile :sinkmode :replace))
         [?cl ?id]
         (src ?cl ?id))))

