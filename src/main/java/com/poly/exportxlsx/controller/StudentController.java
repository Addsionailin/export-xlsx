package com.poly.exportxlsx.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.poly.exportxlsx.entity.Student;
import com.poly.exportxlsx.service.IStudentService;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private IStudentService studentService;

    /**
     * 导出 分文件打包.zip
     */
    @ResponseBody
    @PostMapping("/exportZip")
    public void exportAsync(HttpServletResponse response, Student params) {
        boolean responseCommitted = false;
        ExcelWriter excelWriter = null;
        FileOutputStream excelOut = null;
        FileInputStream excelIn = null;
        ZipOutputStream zipOut = null;
        List<File> tempFiles = new ArrayList<>();
        int pageSize = 1000;
        int maxRecordsPerFile = 1000; // 每个Excel文件最大记录数

        try {
            if (params == null) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter().write("请求参数不能为空");
                return;
            }

            // 设置ZIP响应头
            response.setContentType("application/zip");
            String fileName = "学生数据_" + System.currentTimeMillis() + ".zip";
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 创建ZIP输出流
            zipOut = new ZipOutputStream(response.getOutputStream());
            responseCommitted = true;

            // 分页导出变量
            String lastIccid = null;
            long totalExported = 0;
            int fileIndex = 1;
            long lastFlushTime = System.currentTimeMillis();
            File currentTempFile = null;
            int recordsInCurrentFile = 0;

            while (true) {
                // 构建查询
                LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>(params);
                if (lastIccid != null) {
                    wrapper.gt(Student::getId, lastIccid);
                }
                wrapper.orderByAsc(Student::getId);
                wrapper.last("LIMIT " + pageSize);

                // 查询数据
                List<Student> records = studentService.list(wrapper);
                if (records.isEmpty()) {
                    break;
                }

                if (excelWriter == null || recordsInCurrentFile + records.size() > maxRecordsPerFile) {
                    // 关闭前一个文件
                    if (excelWriter != null) {
                        excelWriter.finish();
                        excelOut.flush();
                        excelOut.close();

                        // 将文件添加到ZIP
                        addFileToZip(zipOut, currentTempFile, "学生数据_" + fileIndex + ".xlsx");
                        tempFiles.add(currentTempFile);
                        fileIndex++;
                    }

                    // 创建新临时文件
                    currentTempFile = File.createTempFile("temp_excel_", ".xlsx");
                    currentTempFile.deleteOnExit();
                    excelOut = new FileOutputStream(currentTempFile);

                    // 创建新Excel写入器
                    excelWriter = EasyExcel.write(excelOut, Student.class)
                            .autoCloseStream(false)
                            .build();
                }

                // 写入数据
                WriteSheet writeSheet = EasyExcel.writerSheet("学生数据").build();
                excelWriter.write(records, writeSheet);
                totalExported += records.size();
                recordsInCurrentFile += records.size();
                lastIccid = records.get(records.size() - 1).getId();

                // 定期刷新（每5秒或每5000条）
                if (totalExported % 5000 == 0 ||
                        System.currentTimeMillis() - lastFlushTime > 5000) {
                    excelOut.flush();
                    lastFlushTime = System.currentTimeMillis();
                    System.out.println("已导出 " + totalExported + " 条记录");
                }
            }

            // 处理最后一个文件
            if (excelWriter != null && recordsInCurrentFile > 0) {
                excelWriter.finish();
                excelOut.flush();
                excelOut.close();
                addFileToZip(zipOut, currentTempFile, "学生数据_" + fileIndex + ".xlsx");
                tempFiles.add(currentTempFile);
            }

            zipOut.finish();
            System.out.println("导出完成，共 " + totalExported + " 条记录，生成 "  +
                    (recordsInCurrentFile > 0 ? fileIndex : fileIndex - 1) + " 个文件");

        } catch (ClientAbortException e) {
            System.out.println("客户端中止下载: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("导出失败: " + e);
            if (!responseCommitted) {
                sendErrorResponse(response, "导出失败: " + e.getMessage());
            }
        } finally {
            // 关闭资源并清理临时文件
            closeResources(excelWriter, excelOut, excelIn, zipOut, tempFiles);
        }
    }

    // 添加文件到ZIP包
    private void addFileToZip(ZipOutputStream zipOut, File file, String entryName) throws IOException {
        zipOut.putNextEntry(new ZipEntry(entryName));
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
        }
        zipOut.closeEntry();
    }

    // 安全关闭资源和清理临时文件
    private void closeResources(ExcelWriter excelWriter, OutputStream excelOut,
                                InputStream excelIn, ZipOutputStream zipOut,
                                List<File> tempFiles) {
        try {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        } catch (Exception e) {
            System.out.println("关闭ExcelWriter失败: " + e);
        }

        try {
            if (zipOut != null) {
                zipOut.close();
            }
        } catch (IOException e) {
            System.out.println("关闭ZIP输出流失败: " + e);
        }

        try {
            if (excelOut != null) {
                excelOut.close();
            }
        } catch (IOException e) {
            System.out.println("关闭Excel输出流失败: " + e);
        }

        try {
            if (excelIn != null) {
                excelIn.close();
            }
        } catch (IOException e) {
            System.out.println("关闭Excel输入流失败: " + e);
        }

        // 清理所有临时文件
        for (File tempFile : tempFiles) {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.out.println("删除临时文件失败: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    // 导出错误返回
    private void sendErrorResponse(HttpServletResponse response, String message) {
        try {
            response.resetBuffer();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + message + "\"}");
        } catch (IOException ex) {
            System.out.println("发送错误响应失败: " + ex);
        }
    }
}
