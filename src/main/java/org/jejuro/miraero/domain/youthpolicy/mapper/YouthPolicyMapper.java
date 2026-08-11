package org.jejuro.miraero.domain.youthpolicy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyDetailQueryResult;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyListQueryResult;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicy;

@Mapper
public interface YouthPolicyMapper {

    int upsert(YouthPolicy youthPolicy);

    List<YouthPolicyListQueryResult> findYouthPolicies(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("search") String search,
            @Param("age") Integer age,
            @Param("monthlyIncome") Long monthlyIncome,
            @Param("offset") Long offset,
            @Param("size") Integer size
    );

    long countYouthPolicies(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("search") String search,
            @Param("age") Integer age,
            @Param("monthlyIncome") Long monthlyIncome
    );

    YouthPolicyDetailQueryResult findYouthPolicyById(
            @Param("youthPolicyId") Long youthPolicyId
    );
}
