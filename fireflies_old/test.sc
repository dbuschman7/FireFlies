object test {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  val rows = 4                                    //> rows  : Int = 4
  val cols = 4                                    //> cols  : Int = 4

  for (
    x <- 0 to (rows - 1);
    y <- 0 to (cols - 1)

  ) {
    ???                                           //> scala.NotImplementedError: an implementation is missing
                                                  //| 	at scala.Predef$.$qmark$qmark$qmark(Predef.scala:225)
                                                  //| 	at test$$anonfun$main$1$$anonfun$apply$mcV$sp$1$$anonfun$apply$mcVI$sp$1
                                                  //| .apply(test.scala:12)
                                                  //| 	at test$$anonfun$main$1$$anonfun$apply$mcV$sp$1$$anonfun$apply$mcVI$sp$1
                                                  //| .apply(test.scala:9)
                                                  //| 	at scala.collection.immutable.Range.foreach(Range.scala:166)
                                                  //| 	at test$$anonfun$main$1$$anonfun$apply$mcV$sp$1.apply$mcVI$sp(test.scala
                                                  //| :9)
                                                  //| 	at scala.collection.immutable.Range.foreach$mVc$sp(Range.scala:166)
                                                  //| 	at test$$anonfun$main$1.apply$mcV$sp(test.scala:8)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at test$.main(test.scala:1)
                                                  //| 	at test.main
                                                  //| Output exceeds cutoff limit.
  }
}