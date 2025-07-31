package com.smc.recurring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smc.recurring.dto.Orders;
import com.smc.recurring.entity.RecurringPaymentEntity;
import com.smc.recurring.repository.RecurringPaymentDAO;
import com.smc.recurring.util.ApplicationEnum;
import com.smc.recurring.util.ApplicationUtil;
import com.smc.recurring.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class CSVService {

    @Autowired
    RecurringPaymentDAO recurringPaymentDAO;

    public void saveCSV(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            Iterable<CSVRecord> records = format.parse(reader);

            for (CSVRecord record : records) {

                try {
                    String contact = record.get("user_id");
                    String orderId = record.get("order_id");
                    String paymentId = record.get("payment_Id");
                    String tokenId = record.get("token_Id");
                    String amount = record.get("amount");
                    String customerId = record.get("customer_id");
                    String createdDate = record.get("created_on");

                    RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByContact(contact);
                    if (ObjectUtils.isEmpty(recurringPaymentEntity)) {
                        log.info("Recurring Payment Entity is NULL - " + record);
                        continue;
                    }

                    if (tokenId.equalsIgnoreCase("NULL")) {
                        if (recurringPaymentEntity.getTokenId() == null) {
                            log.info("Token Id in database also null and excel also null - " + customerId);
                            //Updating Orders array using OrderId
                            List<String> orders = recurringPaymentEntity.getOrders();
                            List<String> ans = new ArrayList<>();

                            String finalTokenId = tokenId;
                            orders.stream().forEach(order -> {
                                try {
                                    Orders a = ApplicationUtil.convertToObject(order, Orders.class);
                                    if (a.getOrderId().equals(orderId)) {

                                        a.setPaymentId(paymentId);
                                        a.setTokenId(finalTokenId.equalsIgnoreCase("NULL") ? null : finalTokenId);
                                        a.setOrderStatus(ApplicationEnum.OrderStatus.SUCCESS.name());
                                        //a.setErrDescription(errorDesc);
                                        a.setMonth(createdDate);

                                        ans.add(ApplicationUtil.convertToString(a));
                                    } else {
                                        ans.add(order);
                                    }

                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            recurringPaymentEntity.setOrders(ans);
                            recurringPaymentDAO.save(recurringPaymentEntity);
                            log.info("Successfully saved only Orders field");
                            continue;
                        }
                        tokenId = recurringPaymentEntity.getTokenId();
                    }

                    //Updating Orders array using OrderId
                    List<String> orders = recurringPaymentEntity.getOrders();
                    List<String> ans = new ArrayList<>();

                    String finalTokenId = tokenId;
                    orders.stream().forEach(order -> {
                        try {
                            Orders a = ApplicationUtil.convertToObject(order, Orders.class);
                            if (a.getOrderId().equals(orderId)) {

                                a.setPaymentId(paymentId);
                                a.setTokenId(finalTokenId.equalsIgnoreCase("NULL") ? null : finalTokenId);
                                a.setOrderStatus(ApplicationEnum.OrderStatus.SUCCESS.name());
                                //a.setErrDescription(errorDesc);
                                a.setMonth(createdDate);

                                ans.add(ApplicationUtil.convertToString(a));
                            } else {
                                ans.add(order);
                            }

                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });


                    String mandateStaus = recurringPaymentEntity.getMandateStatus();
                    String frequency = recurringPaymentEntity.getFrequency();
                    if (!mandateStaus.equalsIgnoreCase(ApplicationEnum.MandateStatus.UNSUBSCRIBED.name())) {
                        recurringPaymentEntity.setMandateStatus(ApplicationEnum.MandateStatus.SUBSCRIBED.name());
                        recurringPaymentEntity.setNachStatus(ApplicationEnum.NachStatus.ACTIVE.name());
                        recurringPaymentEntity.setOrderId(orderId);
                        recurringPaymentEntity.setTokenId(tokenId);
                        recurringPaymentEntity.setAmount(Double.valueOf(amount));
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(createdDate, frequency));
                        recurringPaymentEntity.setRetryAttempts(0);
                    }

                    recurringPaymentEntity.setOrders(ans);


                    recurringPaymentDAO.save(recurringPaymentEntity);

                } catch (Exception e) {
                    log.info("Some error occured while inserting - " + e.getMessage(), " ", record);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV: " + e.getMessage());
        }
    }


    public void changeData() {

        List<RecurringPaymentEntity> orders = recurringPaymentDAO.findByFrequency("yearly");

        log.info("The list is - " + orders.size());
        AtomicInteger ctr = new AtomicInteger();
        orders.stream().forEach(order -> {

            try {
                if (order.getMandateStatus().equalsIgnoreCase(ApplicationEnum.MandateStatus.SUBSCRIBED.name()) &&
                        order.getNachStatus().equalsIgnoreCase(ApplicationEnum.NachStatus.ACTIVE.name())) {


                    ctr.getAndIncrement();
                    order.setDueDate(DateUtil.getDueDate(order.getDueDate(), "minusone"));
                    recurringPaymentDAO.save(order);

                }
            } catch (Exception e) {
                log.info("Some error occured - " + e.getMessage());
            }
        });

        log.info("The counter is - " + ctr);

    }
}
