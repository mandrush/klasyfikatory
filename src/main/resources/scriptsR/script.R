library(tidyverse )

MyData <- read.csv(file="C:/Users/Kamil/Desktop/dane/hotels.csv", header=FALSE, sep=",", skip = 1)[ ,c('V7', 'V10')]
MyData["neg"] <- NA
MyData$neg <- "neg"
MyData["pos"] <- NA
MyData$pos <- "pos"

MyDataPositives <- MyData[ ,c('pos', 'V10')]
MyDataPositives <- MyDataPositives[MyDataPositives$V10!='No Positive',]
MyDataNegatives <- MyData[ ,c('neg', 'V7')]
MyDataNegatives <- MyDataNegatives[MyDataNegatives$V7!='No Negative',]

names(MyDataPositives)[1] <- "class"
names(MyDataNegatives)[1] <- "class"

names(MyDataPositives)[2] <- "sentence"
names(MyDataNegatives)[2] <- "sentence"

indexesNeg = sample(1:nrow(MyDataNegatives), size=0.3*nrow(MyDataNegatives))
indexesPos = sample(1:nrow(MyDataPositives), size=0.3*nrow(MyDataPositives))

x <- MyDataNegatives[indexesNeg,]
y <- MyDataPositives[indexesPos,]

hotelTrain <- rbind(MyDataNegatives[indexesNeg,], MyDataPositives[indexesPos,])
hotelTest <- rbind(MyDataNegatives[-indexesNeg,], MyDataPositives[-indexesPos,])

write.table(hotelTrain, file="C:/Users/Kamil/Desktop/dane/hotel_train.csv", sep=" ", col.names = FALSE, row.names = FALSE, quote = FALSE)
write.table(hotelTest, file="C:/Users/Kamil/Desktop/dane/hotel_test.csv", sep="|", col.names = FALSE, row.names = FALSE, quote = FALSE)