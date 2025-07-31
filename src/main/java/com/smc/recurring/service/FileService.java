package com.smc.recurring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smc.recurring.dto.Orders;
import com.smc.recurring.entity.RecurringPaymentEntity;
import com.smc.recurring.util.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class FileService {

    public ByteArrayInputStream exportToExcel(List<RecurringPaymentEntity> dataList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("UserData");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Phone");
            header.createCell(2).setCellValue("Mandate Status");
            header.createCell(3).setCellValue("NACH Status");
            header.createCell(4).setCellValue("Order Status");
            header.createCell(5).setCellValue("Date");
            header.createCell(6).setCellValue("Amount");

            AtomicInteger rowIdx = new AtomicInteger(1);
            for (RecurringPaymentEntity data : dataList) {
                List<String> orders = data.getOrders();
                List<Orders> ans = new ArrayList<>();
                orders.stream().forEach(order -> {
                    try {
                        ans.add(ApplicationUtil.convertToObject(order, Orders.class));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

                ans.stream().forEach(order -> {
                    Row row = sheet.createRow(rowIdx.getAndIncrement());
                    row.createCell(0).setCellValue(data.getName());
                    row.createCell(1).setCellValue(data.getContact());
                    row.createCell(2).setCellValue(data.getMandateStatus());
                    row.createCell(3).setCellValue(data.getNachStatus());
                    row.createCell(4).setCellValue(order.getOrderStatus());
                    row.createCell(5).setCellValue(order.getMonth());
                    row.createCell(6).setCellValue(data.getAmount());
                });
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }


}
