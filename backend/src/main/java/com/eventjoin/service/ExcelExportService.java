package com.eventjoin.service;

import com.eventjoin.entity.Event;
import com.eventjoin.entity.Registration;
import com.eventjoin.entity.User;
import com.eventjoin.exception.BusinessException;
import com.eventjoin.repository.EventRepository;
import com.eventjoin.repository.RegistrationRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {
    
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    
    public ExcelExportService(RegistrationRepository registrationRepository,
                              EventRepository eventRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
    }
    
    public byte[] exportRegistrationsToExcel(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        List<Registration> registrations = registrationRepository.findAllActiveByEventId(eventId);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("报名列表");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            createTitleRow(sheet, event, headerStyle);
            
            String[] headers = {"序号", "用户名", "邮箱", "手机号", "报名时间"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 3;
            int index = 1;
            
            for (Registration registration : registrations) {
                User user = registration.getUser();
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(index++);
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(3).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                
                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(registration.getRegisteredAt().format(formatter));
                dateCell.setCellStyle(dateStyle);
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败：" + e.getMessage());
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        return style;
    }
    
    private void createTitleRow(Sheet sheet, Event event, CellStyle headerStyle) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        Row titleRow1 = sheet.createRow(0);
        Cell titleCell1 = titleRow1.createCell(0);
        titleCell1.setCellValue("活动报名列表 - " + event.getTitle());
        
        Row titleRow2 = sheet.createRow(1);
        Cell titleCell2 = titleRow2.createCell(0);
        titleCell2.setCellValue("活动时间：" + event.getEventDateTime().format(formatter) + 
                               " | 报名人数：" + event.getCurrentParticipants() + "/" + event.getMaxParticipants());
    }
}
