///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package mono.backend.repository;
//
//import mono.backend.bean.SmsContent;
//import java.util.LinkedList;
//import java.util.List;
//
//
//public class SmsContentDao  extends GenericDao<SmsContent> {
//
//    public SmsContent getActiveContent(){
//        List<String> columnNames = new LinkedList<>();
//        columnNames.add("status");
//        List<Object> values = new LinkedList<>();
//        values.add("1");
//        
//        return findByColumNames(columnNames, values);
//    }
//}
