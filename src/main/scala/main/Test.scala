package main

import curlrequests.CurlByDatabase
import googleapi.GoogleSetup
import models.QA

//sbt "run-main main.Test"
object Test {
    val inst = new CurlByDatabase(QA) //QA - Qa database
    val curlResults = inst.getCurlResults
    val successResults = inst.getSuccessResults

    GoogleSetup.printCurlResults(curlResults, successResults)
}
