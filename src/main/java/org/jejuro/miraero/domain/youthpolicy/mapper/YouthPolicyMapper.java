package org.jejuro.miraero.domain.youthpolicy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyDetailQueryResult;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyListQueryResult;

@Mapper
public interface YouthPolicyMapper {

    List<YouthPolicyListQueryResult> findYouthPolicies(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("search") String search,
            @Param("offset") Long offset,
            @Param("size") Integer size
    );

    long countYouthPolicies(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("search") String search
    );

    YouthPolicyDetailQueryResult findYouthPolicyById(
            @Param("youthPolicyId") Long youthPolicyId
    );
}
