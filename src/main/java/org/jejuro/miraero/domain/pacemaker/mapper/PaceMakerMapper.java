package org.jejuro.miraero.domain.pacemaker.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;

@Mapper
public interface PaceMakerMapper {

  AutoSaving findByUserId(@Param("userId") Long userId);
}
