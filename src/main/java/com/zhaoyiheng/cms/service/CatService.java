package com.zhaoyiheng.cms.service;

import java.util.List;

import com.zhaoyiheng.cms.entity.Cat;

public interface CatService {
	
	/**
	 * 根据频道去获取下边的分类
	 * @param id
	 * @return
	 */
	List<Cat> getListByChnlId(Integer id);  
	

}
