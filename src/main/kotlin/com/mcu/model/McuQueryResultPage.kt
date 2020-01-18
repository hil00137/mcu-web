package com.mcu.model

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage
import java.io.Serializable

class McuQueryResultPage<T> : QueryResultPage<T> , Serializable {
    constructor(queryResultPage: QueryResultPage<T>) {
        this.results = queryResultPage.results
        this.lastEvaluatedKey = queryResultPage.lastEvaluatedKey
    }
}