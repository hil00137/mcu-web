package com.mcu.repository

import com.mcu.model.Board
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository


interface BoardRepository : PagingAndSortingRepository<Board, String> {
    fun countByType(type : String) : Long
    fun findAllByType(type: String, pageable: Pageable) : List<Board>
}