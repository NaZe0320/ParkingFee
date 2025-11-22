package com.naze.parkingfee.data.datasource.local.ocr

import com.naze.parkingfee.presentation.ui.screens.parkinglots.add.AddParkingLotContract
import java.util.UUID
import kotlin.math.abs

/**
 * 주차 요금표 OCR 파싱 유틸리티
 * ML Kit이 인식한 텍스트를 분석하여 FeeRow 리스트로 변환합니다.
 */
object ParkingFeeParser {

    /**
     * ML Kit의 Text 객체나 String을 파싱하여 FeeRow 리스트를 반환합니다.
     * 
     * @param text 인식된 텍스트 (String 또는 ML Kit Text 객체의 fullText)
     * @param textBlocks 텍스트 블록 리스트 (Y좌표 정보 포함, 선택사항)
     * @return 파싱된 FeeRow 리스트와 일 최대 요금
     */
    fun parse(
        text: String,
        textBlocks: List<OcrProcessor.TextBlock> = emptyList()
    ): ParsingResult {
        if (text.isBlank()) {
            return ParsingResult(
                feeRows = emptyList(),
                dailyMaxFee = null,
                isSuccess = false
            )
        }

        // 텍스트 정제: 줄 단위로 분리
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 시간과 금액을 찾기 위한 정규표현식
        val timePattern = Regex("""(\d+)\s*(분|시간)""")
        val feePattern = Regex("""(\d{1,3}(?:,\d{3})*)\s*원""")

        // 각 줄에서 시간과 금액 추출
        val extractedData = mutableListOf<ExtractedFeeData>()

        lines.forEachIndexed { lineIndex, line ->
            // 시간 찾기
            val timeMatches = timePattern.findAll(line)
            timeMatches.forEach { match ->
                val value = match.groupValues[1].toIntOrNull() ?: return@forEach
                val unit = match.groupValues[2]
                val minutes = if (unit == "시간") value * 60 else value
                
                extractedData.add(
                    ExtractedFeeData(
                        lineIndex = lineIndex,
                        lineText = line,
                        minutes = minutes,
                        fee = null,
                        yCoordinate = null,
                        keywords = extractKeywords(line)
                    )
                )
            }

            // 금액 찾기
            val feeMatches = feePattern.findAll(line)
            feeMatches.forEach { match ->
                val feeText = match.groupValues[1].replace(",", "")
                val fee = feeText.toIntOrNull() ?: return@forEach
                
                extractedData.add(
                    ExtractedFeeData(
                        lineIndex = lineIndex,
                        lineText = line,
                        minutes = null,
                        fee = fee,
                        yCoordinate = null,
                        keywords = extractKeywords(line)
                    )
                )
            }
        }

        // Y좌표 정보가 있으면 활용
        if (textBlocks.isNotEmpty()) {
            updateYCoordinates(extractedData, textBlocks, lines)
        }

        // 시간과 금액을 쌍으로 묶기
        val pairedData = pairTimeAndFee(extractedData)

        // 하루 최대 요금 찾기 (정규표현식 + 키워드 기반)
        val dailyMaxFee = findDailyMaxFee(lines, pairedData)

        // FeeRow로 변환
        val feeRows = mutableListOf<AddParkingLotContract.FeeRow>()

        pairedData.forEach { data ->
            // 하루 최대 요금으로 이미 처리된 데이터는 FeeRow에서 제외
            val isDailyMaxFee = dailyMaxFee != null && data.fee == dailyMaxFee &&
                (data.keywords.any { it in setOf("일최대", "일 최대", "종일", "하루", "1일", "24시간", "일일") } ||
                 data.keywords.contains("최대") || data.keywords.contains("상한") || data.keywords.contains("한도"))
            
            if (isDailyMaxFee) {
                return@forEach
            }

            // 시간과 금액이 모두 있어야 FeeRow 생성
            val minutes = data.minutes ?: return@forEach
            val fee = data.fee ?: return@forEach

            // 최초/기본 키워드가 있으면 첫 번째 항목 (Index 0)
            if (data.keywords.contains("최초") || data.keywords.contains("기본")) {
                feeRows.add(
                    0,
                    AddParkingLotContract.FeeRow(
                        id = UUID.randomUUID().toString(),
                        startTime = 0,
                        endTime = minutes,
                        unitMinutes = minutes,
                        unitFee = fee,
                        isFixedFee = true
                    )
                )
            } else {
                // 초과/이후/추가 키워드가 있거나 그 외의 데이터는 추가 항목
                val lastRow = feeRows.lastOrNull()
                val startTime = lastRow?.endTime ?: 0
                
                feeRows.add(
                    AddParkingLotContract.FeeRow(
                        id = UUID.randomUUID().toString(),
                        startTime = startTime,
                        endTime = null, // 무제한으로 설정 (나중에 사용자가 수정 가능)
                        unitMinutes = if (data.keywords.contains("추가") || data.keywords.contains("초과")) {
                            // 추가 요금인 경우, 간격을 추정 (기본 10분)
                            10
                        } else {
                            // 기본적으로 시간과 동일하게 설정
                            minutes
                        },
                        unitFee = fee,
                        isFixedFee = false
                    )
                )
            }
        }

        // FeeRow가 비어있으면 실패로 간주
        val isSuccess = feeRows.isNotEmpty()

        return ParsingResult(
            feeRows = feeRows,
            dailyMaxFee = dailyMaxFee,
            isSuccess = isSuccess
        )
    }

    /**
     * 줄에서 키워드 추출
     */
    private fun extractKeywords(line: String): Set<String> {
        val keywords = mutableSetOf<String>()
        val lowerLine = line.lowercase()

        if (lowerLine.contains("최초")) keywords.add("최초")
        if (lowerLine.contains("기본")) keywords.add("기본")
        if (lowerLine.contains("초과")) keywords.add("초과")
        if (lowerLine.contains("이후")) keywords.add("이후")
        if (lowerLine.contains("추가")) keywords.add("추가")
        
        // 하루 최대 요금 관련 키워드
        if (lowerLine.contains("일최대") || lowerLine.contains("일 최대")) keywords.add("일최대")
        if (lowerLine.contains("종일")) keywords.add("종일")
        if (lowerLine.contains("하루")) keywords.add("하루")
        if (lowerLine.contains("1일")) keywords.add("1일")
        if (lowerLine.contains("24시간")) keywords.add("24시간")
        if (lowerLine.contains("일일")) keywords.add("일일")
        if (lowerLine.contains("최대")) keywords.add("최대")
        if (lowerLine.contains("상한")) keywords.add("상한")
        if (lowerLine.contains("한도")) keywords.add("한도")

        return keywords
    }

    /**
     * 하루 최대 요금을 찾는 함수
     * 정규표현식과 키워드 기반 검색을 모두 수행합니다.
     */
    private fun findDailyMaxFee(
        lines: List<String>,
        pairedData: List<PairedFeeData>
    ): Int? {
        // 정규표현식 패턴과 해당 패턴에서 금액이 있는 그룹 인덱스
        val patternsWithFeeGroup = listOf(
            // "하루 최대 10,000원", "1일 최대 15,000원" 등 - 그룹 3이 금액
            Pair(Regex("""(하루|1일|일일|일)\s*(최대|상한|한도)?\s*(\d{1,3}(?:,\d{3})*)\s*원""", RegexOption.IGNORE_CASE), 3),
            // "10,000원 하루 최대", "15,000원 1일 최대" 등 - 그룹 1이 금액
            Pair(Regex("""(\d{1,3}(?:,\d{3})*)\s*원\s*(하루|1일|일일|일)\s*(최대|상한|한도)?""", RegexOption.IGNORE_CASE), 1),
            // "하루 최대 요금 10,000원" 등 - 그룹 3이 금액
            Pair(Regex("""(하루|1일|일일|일)\s*(최대|상한|한도)?\s*요금\s*(\d{1,3}(?:,\d{3})*)\s*원""", RegexOption.IGNORE_CASE), 3),
            // "24시간 최대 12,000원" 등 - 그룹 2가 금액
            Pair(Regex("""24\s*시간\s*(최대|상한|한도)?\s*(\d{1,3}(?:,\d{3})*)\s*원""", RegexOption.IGNORE_CASE), 2)
        )

        // 정규표현식으로 먼저 검색
        for (line in lines) {
            for ((pattern, feeGroupIndex) in patternsWithFeeGroup) {
                val match = pattern.find(line)
                if (match != null && match.groupValues.size > feeGroupIndex) {
                    val feeText = match.groupValues[feeGroupIndex].replace(",", "").trim()
                    val fee = feeText.toIntOrNull()
                    if (fee != null && fee > 0) {
                        return fee
                    }
                }
            }
        }

        // 키워드 기반 검색 (기존 로직 개선)
        val dailyMaxFeeKeywords = setOf(
            "일최대", "일 최대", "종일", "하루", "1일", "24시간", "일일"
        )
        val maxFeeKeywords = setOf("최대", "상한", "한도")

        for (data in pairedData) {
            val hasDailyKeyword = data.keywords.any { it in dailyMaxFeeKeywords }
            val hasMaxKeyword = data.keywords.any { it in maxFeeKeywords }
            
            // 하루 관련 키워드와 최대 관련 키워드가 모두 있거나, 
            // 하루 관련 키워드만 있어도 하루 최대 요금으로 간주
            if (hasDailyKeyword && data.fee != null) {
                // 최대 관련 키워드가 있으면 더 확실함
                if (hasMaxKeyword) {
                    return data.fee
                }
                // 최대 관련 키워드가 없어도 하루/1일/24시간 등이 있으면 후보로 고려
                // 하지만 다른 조건도 확인 (예: 시간 정보가 없거나 큰 금액인 경우)
                if (data.minutes == null || data.minutes >= 1440) { // 24시간 이상
                    return data.fee
                }
            }
        }

        // 키워드만으로 찾기 (더 관대한 검색)
        for (data in pairedData) {
            if (data.keywords.contains("일최대") || 
                data.keywords.contains("종일") ||
                data.keywords.contains("일 최대") ||
                (data.keywords.contains("하루") && data.keywords.contains("최대")) ||
                (data.keywords.contains("1일") && data.keywords.contains("최대"))) {
                data.fee?.let { return it }
            }
        }

        return null
    }

    /**
     * Y좌표 정보 업데이트 (텍스트 블록 정보 활용)
     */
    private fun updateYCoordinates(
        extractedData: MutableList<ExtractedFeeData>,
        textBlocks: List<OcrProcessor.TextBlock>,
        lines: List<String>
    ) {
        // 각 줄의 Y좌표를 찾기
        val lineYCoordinates = mutableMapOf<Int, Int?>()

        textBlocks.forEach { block ->
            block.lines.forEachIndexed { blockLineIndex, blockLineText ->
                val lineIndex = lines.indexOfFirst { it.contains(blockLineText, ignoreCase = true) }
                if (lineIndex >= 0) {
                    val y = block.boundingBox?.centerY()
                    lineYCoordinates[lineIndex] = y
                }
            }
        }

        // 추출된 데이터에 Y좌표 할당
        extractedData.forEach { data ->
            data.yCoordinate = lineYCoordinates[data.lineIndex]
        }
    }

    /**
     * 시간과 금액을 쌍으로 묶기
     * 같은 줄이나 가까운 Y좌표에 있는 시간과 금액을 매칭합니다.
     */
    private fun pairTimeAndFee(extractedData: List<ExtractedFeeData>): List<PairedFeeData> {
        val paired = mutableListOf<PairedFeeData>()
        val used = BooleanArray(extractedData.size)

        extractedData.forEachIndexed { index, data ->
            if (used[index]) return@forEachIndexed

            // 시간 데이터인 경우
            if (data.minutes != null && data.fee == null) {
                // 같은 줄에 금액이 있는지 확인
                val sameLineFeeIndex = extractedData.withIndex().indexOfFirst { (idx, it) ->
                    !used[idx] &&
                    idx != index &&
                    it.lineIndex == data.lineIndex && 
                    it.fee != null 
                }

                if (sameLineFeeIndex >= 0) {
                    used[index] = true
                    used[sameLineFeeIndex] = true
                    val sameLineFee = extractedData[sameLineFeeIndex]
                    paired.add(
                        PairedFeeData(
                            minutes = data.minutes,
                            fee = sameLineFee.fee,
                            keywords = data.keywords + sameLineFee.keywords,
                            yCoordinate = data.yCoordinate ?: sameLineFee.yCoordinate
                        )
                    )
                } else {
                    // 가까운 Y좌표에 금액이 있는지 확인
                    val nearbyFeeIndex = extractedData.withIndex().indexOfFirst { (idx, it) ->
                        !used[idx] &&
                        idx != index &&
                        it.fee != null &&
                        areClose(data.yCoordinate, it.yCoordinate)
                    }

                    if (nearbyFeeIndex >= 0) {
                        used[index] = true
                        used[nearbyFeeIndex] = true
                        val nearbyFee = extractedData[nearbyFeeIndex]
                        paired.add(
                            PairedFeeData(
                                minutes = data.minutes,
                                fee = nearbyFee.fee,
                                keywords = data.keywords + nearbyFee.keywords,
                                yCoordinate = data.yCoordinate ?: nearbyFee.yCoordinate
                            )
                        )
                    }
                }
            }
            // 금액만 있는 경우 (시간 없이)
            else if (data.fee != null && data.minutes == null) {
                // 같은 줄에 시간이 있는지 확인
                val sameLineTimeIndex = extractedData.withIndex().indexOfFirst { (idx, it) ->
                    !used[idx] &&
                    idx != index &&
                    it.lineIndex == data.lineIndex && 
                    it.minutes != null 
                }

                if (sameLineTimeIndex >= 0) {
                    used[index] = true
                    used[sameLineTimeIndex] = true
                    val sameLineTime = extractedData[sameLineTimeIndex]
                    paired.add(
                        PairedFeeData(
                            minutes = sameLineTime.minutes,
                            fee = data.fee,
                            keywords = data.keywords + sameLineTime.keywords,
                            yCoordinate = data.yCoordinate ?: sameLineTime.yCoordinate
                        )
                    )
                } else {
                    // 가까운 Y좌표에 시간이 있는지 확인
                    val nearbyTimeIndex = extractedData.withIndex().indexOfFirst { (idx, it) ->
                        !used[idx] &&
                        idx != index &&
                        it.minutes != null &&
                        areClose(data.yCoordinate, it.yCoordinate)
                    }

                    if (nearbyTimeIndex >= 0) {
                        used[index] = true
                        used[nearbyTimeIndex] = true
                        val nearbyTime = extractedData[nearbyTimeIndex]
                        paired.add(
                            PairedFeeData(
                                minutes = nearbyTime.minutes,
                                fee = data.fee,
                                keywords = data.keywords + nearbyTime.keywords,
                                yCoordinate = data.yCoordinate ?: nearbyTime.yCoordinate
                            )
                        )
                    } else {
                        // 시간 없이 금액만 있는 경우, 기본 시간(30분)으로 추정
                        used[index] = true
                        paired.add(
                            PairedFeeData(
                                minutes = 30, // 기본값
                                fee = data.fee,
                                keywords = data.keywords,
                                yCoordinate = data.yCoordinate
                            )
                        )
                    }
                }
            }
        }

        return paired
    }

    /**
     * 두 Y좌표가 가까운지 확인 (50픽셀 이내)
     */
    private fun areClose(y1: Int?, y2: Int?): Boolean {
        if (y1 == null || y2 == null) return false
        return abs(y1 - y2) <= 50
    }

    /**
     * 파싱 결과 데이터 클래스
     */
    data class ParsingResult(
        val feeRows: List<AddParkingLotContract.FeeRow>,
        val dailyMaxFee: Int?,
        val isSuccess: Boolean
    )

    /**
     * 추출된 요금 데이터 (내부 사용)
     */
    private data class ExtractedFeeData(
        val lineIndex: Int,
        val lineText: String,
        val minutes: Int?,
        val fee: Int?,
        var yCoordinate: Int?,
        val keywords: Set<String>
    )

    /**
     * 쌍으로 묶인 요금 데이터 (내부 사용)
     */
    private data class PairedFeeData(
        val minutes: Int?,
        val fee: Int?,
        val keywords: Set<String>,
        val yCoordinate: Int?
    )
}

