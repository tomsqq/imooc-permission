package com.lwq.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.lwq.dao.SysAclModuleMapper;
import com.lwq.dao.SysDeptMapper;
import com.lwq.dto.AclModuleLevelDto;
import com.lwq.dto.DeptLevelDto;
import com.lwq.model.SysAclModule;
import com.lwq.model.SysDept;
import com.lwq.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author: Lwq
 * @Date: 2019/3/11 22:28
 * @Version 1.0
 * @Describe
 */
@Service
public class SysTreeService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    public List<AclModuleLevelDto> aclModuleTree(){
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();

        for(SysAclModule aclModule:aclModuleList){
            AclModuleLevelDto dto = AclModuleLevelDto.adapt(aclModule);
            dtoList.add(dto);
        }
        return aclModuleListToTree(dtoList);
    }


    public List<DeptLevelDto> deptTree(){
        List<SysDept> deptList = sysDeptMapper.getAllDept();
        List<DeptLevelDto> dtoList = Lists.newArrayList();

        for(SysDept dept:deptList){
            DeptLevelDto dto = DeptLevelDto.adapt(dept);
            dtoList.add(dto);
        }
        return deptListToTree(dtoList);
    }

    public List<DeptLevelDto> deptListToTree(List<DeptLevelDto> deptLevelList){
        if(CollectionUtils.isEmpty(deptLevelList)){
            return Lists.newArrayList();
        }
//        level -> [dept1,dept2....]
//        Map<String,List<Object>>
        Multimap<String,DeptLevelDto> levelDtoMultiMap = ArrayListMultimap.create();
        List<DeptLevelDto> rootList = Lists.newArrayList();

        for(DeptLevelDto dto:deptLevelList){
            levelDtoMultiMap.put(dto.getLevel(),dto);
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }

        //按照seq排序,处理第一层
        Collections.sort(rootList,deptSeqComparator);

        //递归生成树（从第二层开始）
        transformDeptTree(rootList,LevelUtil.ROOT,levelDtoMultiMap);
        return rootList;
    }

    //level:0 0,all
    public void transformDeptTree(List<DeptLevelDto> deptLevelList,String level,Multimap<String,DeptLevelDto> levelDtoMultiMap){
        for(int  i = 0; i < deptLevelList.size(); i++){
            //遍历该层的每个元素
            DeptLevelDto deptLevelDto = deptLevelList.get(i);
            //处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level,deptLevelDto.getId());
            //处理下一层
            List<DeptLevelDto> temoDeptList = (List<DeptLevelDto>) levelDtoMultiMap.get(nextLevel);
            if(CollectionUtils.isNotEmpty(temoDeptList)){
                //排序
                Collections.sort(temoDeptList,deptSeqComparator);

                //设置下一层
                deptLevelDto.setDeptList(temoDeptList);

                //处理下一层
                transformDeptTree(temoDeptList,nextLevel,levelDtoMultiMap);
            }
        }
    }

    public Comparator<DeptLevelDto> deptSeqComparator = new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };

    private List<AclModuleLevelDto> aclModuleListToTree( List<AclModuleLevelDto> aclModuleLevelList) {
        if(CollectionUtils.isEmpty(aclModuleLevelList)){
            return Lists.newArrayList();
        }
//        level -> [aclModule1,aclModule2....]
//        Map<String,List<Object>>
        Multimap<String,AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();

        for(AclModuleLevelDto dto:aclModuleLevelList){
            levelAclModuleMap.put(dto.getLevel(),dto);
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }

        //按照seq排序,处理第一层
        Collections.sort(rootList, aclModuleSeqComparator);

        //递归生成树（从第二层开始）
        transformAclModuleTree(rootList,LevelUtil.ROOT,levelAclModuleMap);
        return rootList;
    }

    private void transformAclModuleTree(List<AclModuleLevelDto> aclModuleLevelList, String level, Multimap<String, AclModuleLevelDto> levelAclModuleMap) {
        for(int  i = 0; i < aclModuleLevelList.size(); i++){
            //遍历该层的每个元素
            AclModuleLevelDto aclModuleLevelDto = aclModuleLevelList.get(i);
            //处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level,aclModuleLevelDto.getId());
            //处理下一层
            List<AclModuleLevelDto> tempAclModuleList = (List<AclModuleLevelDto>) levelAclModuleMap.get(nextLevel);
            if(CollectionUtils.isNotEmpty(tempAclModuleList)){
                //排序
                Collections.sort(tempAclModuleList,aclModuleSeqComparator);

                //设置下一层
                aclModuleLevelDto.setAclModuleList(tempAclModuleList);

                //处理下一层
                transformAclModuleTree(tempAclModuleList,nextLevel,levelAclModuleMap);
            }
        }
    }

    public Comparator<AclModuleLevelDto> aclModuleSeqComparator = new Comparator<AclModuleLevelDto>() {
        @Override
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };
}
