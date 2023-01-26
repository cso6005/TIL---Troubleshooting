package io.star.model;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.star.exception.CustomException;
import io.star.exception.UserErrorCode;
import io.star.model.dto.ObserveStarRegionDTO;

/** 로그 처리 예제 - elastic search 꺼졌을 때의 경우 로그 처리를 해보자.*/
@Service
public class ELKService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(ELKService.class);
	
	@Transactional
	public Map<String, Object> getData(String region) throws IOException {
		
		// 로직 수행 시간 측정을 위해 현재 시간 측정
		long startTime = System.currentTimeMillis();

		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));

		String index = "weather";
		GetRequest request = new GetRequest(index, region);
		RequestOptions options = RequestOptions.DEFAULT;
		
		Map<String, Object> sourceAsMap = null;
		
		try {
			GetResponse response = client.get(request, options);
			if (response.isExists()) {
				sourceAsMap = response.getSourceAsMap();			
			} 
		} catch (IOException e) {
			// 서버 꺼졌을 때 로그 처리
			// 로그백에서는 {} 로 변수를 넣기 쉽게 지원해준다. 아래와 같이 쉽게 변수를 넣어 출력할 수 있다.
			LOGGER.warn("[ELKService][getData] - ElasticSearch 9200 서버 꺼져있음. :: Response = ResponseEntity<ErrorResponse> :: {}ms", (System.currentTimeMillis() - startTime));
			throw new CustomException(UserErrorCode.ELK_NOT_CONNECT);
		} finally {
			client.close();
		}

		return sourceAsMap;
	}
	
	

}
