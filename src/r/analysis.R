library(texreg)
source("clean-econ.R")
source("utils.R")

## retrieve cluster count data as data frame, full.data
load("../../data/processed/cluster-count-01.Rdata")

## Create binary variables and screen out early data
data <- full.data[get.year(full.data$date) >= 2008, ]
data$cntry <- ifelse(data$cntry == "mys", 0, 1)

## Merge economic data
data <- merge(data, econ.data, by=c("date"))

## Transform measures so that the tables do not have so many digits
data$s.prop <- data$s.prop * 100
data$price <- data$price / 1000
data$idn.exch <- data$idn.exch * 1000

## Results for proportion variables
data$post <- ifelse(data$date < as.Date("2010-05-20"), 0, 1)
m1 <- lm(s.prop ~ 1 + cntry*post + price + I(price^2), data = data)

data$post <- ifelse(data$date < as.Date("2011-01-01"), 0, 1)
m2 <- lm(s.prop ~ 1 + cntry*post + price + I(price^2), data = data)

data$post <- ifelse(data$date < as.Date("2011-05-20"), 0, 1)
m3 <- lm(s.prop ~ 1 + cntry*post + price + I(price^2), data = data)

create.table(list(m1, m2, m3), "prop-res.tex")

## Results for overall trends
m1 <- lm(total ~ 1 + date + post, data = data)
m2 <- lm(total ~ 1 + cntry*post, data = data)
m3 <- lm(total ~ 1 + price*post, data = data)
m4 <- lm(total ~ 1 + price*cntry*post, data = data)

create.table(list(m1, m2, m3, m4), "total-res.tex")

## Check to see if the distributions of the land characteristics are
## significantly different from one another, split by type of clearing
## activity.

idn <- data[data$cntry == 1, ]
mys <- data[data$cntry == 0, ]

test.means <- function(df, var.name) {
  new <- paste("new.", var.name, sep="")
  old <- paste("old.", var.name, sep="")
  t.test(df[[new]], df[[old]])
}

## test differences in land characteristics between the different
## types of deforestation for Indonesia and Malaysia; store in
## separate lists
land.list <- list(slope="slope", elev="elev", accum="accum")
idn.tests <- lapply(land.list, function(x) {test.means(idn, x)})
mys.tests <- lapply(land.list, function(x) {test.means(mys, x)})

## Example output:
print(idn.tests$elev)
