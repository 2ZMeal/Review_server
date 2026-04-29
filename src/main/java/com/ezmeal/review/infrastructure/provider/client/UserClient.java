package com.ezmeal.review.infrastructure.provider.client;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.review.infrastructure.provider.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    // 리뷰 작성자의 nickname을 가져오기 위해 호출
    @GetMapping("/api/v1/users/{userId}")
    CommonApiResponse<UserResponse> getUser(@PathVariable("userId") String userId);
}
