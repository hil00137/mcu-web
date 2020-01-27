package com.mcu.service

import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.util.IOUtils
import com.mcu.model.Board
import com.mcu.model.HistoryPriority
import com.mcu.model.Image
import com.mcu.repository.ImageRepository
import com.mcu.util.AwsConnector
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.*

@Service
class ImageService {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    @Autowired
    private lateinit var awsConnector: AwsConnector
    @Autowired
    private lateinit var imageRepository: ImageRepository
    @Autowired
    private lateinit var historyService : HistoryService

    private val validExt = arrayOf("jpg", "png", "bmp", "gif")

    /**
     * 이미지 파일 업로드
     */
    fun uploadObject(file: MultipartFile): Map<String, String> {
        val resultMap = HashMap<String, String>()

        val filename = file.originalFilename
        if(filename == null) {
            resultMap["code"] = "fail"
            resultMap["message"] = "알수없는에러"
            return resultMap
        }

        val uuid = UUID.randomUUID().toString().replace("-","")
        val ext = filename.split(".")[1]

        if(!validExt.contains(ext)) {
            resultMap["code"] = "fail"
            resultMap["message"] = "NOTALLOW_$filename"
            return resultMap
        }

        val localDateTime = LocalDateTime.now()
        val year = localDateTime.year
        val month = localDateTime.monthValue
        val day = localDateTime.dayOfMonth


        val bytes = IOUtils.toByteArray(file.inputStream)
        val s3 = this.awsConnector.getS3Connection()
        val metadata  = ObjectMetadata()
        metadata.contentType =  file.contentType
        metadata.contentLength = bytes.size.toLong()

        val bucketName = awsConnector.bucket
        val byteInputStream = ByteArrayInputStream(bytes)
        val key = "$year/$month/$day/$uuid.$ext"
        try {
            s3.putObject(PutObjectRequest(bucketName, key, byteInputStream, metadata))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fileUrl = s3.getUrl(bucketName, key).toString()

        val image = Image(key)
        image.boardId = "UNKNOWN"
        image.regist = localDateTime
        image.url = fileUrl
        image.oriFileName = filename
        image.contentType = file.contentType
        image.userId = SecurityContextHolder.getContext().authentication.principal as String

        imageRepository.save(image)
        historyService.writeHistory("Image Upload : $key", HistoryPriority.USER_REQUEST)
        resultMap["code"] = "success"
        resultMap["message"] = "&sFileName="
        resultMap["message"] += "$filename"
        resultMap["message"] += "&sFileURL="
        resultMap["message"] += fileUrl
        return resultMap
    }

    /**
     * s3에서 image 파일 삭제
     */
    private fun deleteImage(key : String) {
        this.awsConnector.getS3Connection().deleteObject(awsConnector.bucket, key)
    }

    /**
     * 게시글 삭제시 게시글에 등록된 이미지를 전부 삭제
     */
    fun deleteImageRelateBoard(boardId : String) {
        val images = getOriImage(boardId)
        images.forEach {
            key ->
            val image = this.imageRepository.findByKey(key)?: return
            this.imageRepository.delete(image)
            this.deleteImage(key)
        }
    }

    /**
     * save Image
     */
    fun saveImage(oriBoard : Board, newBoard : Board) {
        val oriImages = getOriImage(oriBoard.id!!)
        val newImages = parseImage(newBoard.content)

        // 새글에서 이미지를 추출
        newImages.forEach {
            key ->            
            if(oriImages.contains(key)) {// 기존리스트에 이미 있는 이미지일 경우 (기존리스트에서 제거만)
                oriImages.remove(key)
            } else {//기존리스트에 없는경우 boardId를 지정.
                val image = this.imageRepository.findByKey(key) ?: return
                if(oriBoard.id == "TEMP") {//새글 등록일경우
                    image.boardId = newBoard.id
                } else {//기존의 글 변경일 경우
                    image.boardId = oriBoard.id
                }
                this.imageRepository.save(image)
            }
        }
        oriImages.forEach {// 기존리스트에 남은경우 새글에는 없는 이미지이므로 삭제처리
            key ->
            this.deleteImage(key)
            this.imageRepository.delete(Image(key))
        }
    }

    /**
     * 내용으로부터 이미지 태그 파싱
     */
    private fun parseImage(content : String): ArrayList<String> {
        val result = ArrayList<String>()
        val doc = Jsoup.parseBodyFragment(content)
        doc.getElementsByTag("img").forEach {el ->
            val temp = el.attr("src").split("/")
            if(temp.size != 7) return@forEach
            val domain = temp[2]
            if(!domain.contains(awsConnector.bucket))  return@forEach
            val year = temp[3]
            val month = temp[4]
            val day = temp[5]
            val file = temp[6]
            val key = "$year/$month/$day/$file"
            result.add(key)
        }
        return result
    }

    private fun getOriImage(boardId : String): ArrayList<String> {
        val result = ArrayList<String>()
        this.imageRepository.findByBoardId(boardId).results.forEach {
            result.add(it.key!!)
        }
        return result
    }
}