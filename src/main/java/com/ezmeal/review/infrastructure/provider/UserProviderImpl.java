package com.ezmeal.review.infrastructure.provider;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.review.domain.provider.UserData;
import com.ezmeal.review.domain.provider.UserProvider;
import com.ezmeal.review.infrastructure.provider.client.UserClient;
import com.ezmeal.review.infrastructure.provider.client.dto.UserResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProviderImpl implements UserProvider {

    private final UserClient userClient;

    @Override
    public UserData getUser(String userId) {
        try {
            CommonApiResponse<UserResponse> res = userClient.getUser(userId);

            // CommonApiResponse 내부의 data 필드가 null인지 체크
            if (res == null || res.getData() == null) {
                log.error("사용자 정보가 존재하지 않습니다. userId: {}", userId);
                throw new IllegalStateException("사용자 조회 실패: 가용한 정보가 없습니다.");
            }

            UserResponse data = res.getData();

            // 도메인 계층에서 요구하는 DTO로 변환하여 리턴
            return new UserData(
                    data.nickname()
            );

        } catch (FeignException.NotFound e) {
            log.warn("사용자를 찾을 수 없습니다. userId={}", userId);
            throw new IllegalStateException("사용자 조회 실패: 리소스를 찾을 수 없습니다.");
        } catch (FeignException e) {
            log.error("User 서비스 호출 실패(사용자 조회): userId={}, Error={}", userId, e.getMessage());
            throw new RuntimeException("사용자 정보 조회 중 외부 서비스 호출에 실패했습니다.", e);
        }
    }
}
