/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.util;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap descriptions code return for CP
 * @version 1.0
 * @author langiac
 * @since 10 10 2015
 */
public class StaticCode {
    private static final String code0 ="Giao dịch thành công";
    private static final String code1 ="Không tìm thấy số điện thoại";
    private static final String code4 ="IP Client không nằm trong dải IP Pool";
    private static final String code11 ="Thiếu tham số";
    private static final String code13 ="Thiếu trường amount";
    private static final String code14 ="Thiếu trường cp request id";
    private static final String code15 ="Thiếu trường value";
    private static final String code16 ="Thiếu trường aes key";
    private static final String code17 ="Thiếu trường name item";
    private static final String code18 ="Thiếu trường category item";
    private static final String code22 ="CP code không hợp lệ/không active";
    private static final String code23 ="Thanh toán không hợp lệ";
    private static final String code24 ="Không có giao dịch confirm trước đó";
    private static final String code25 ="CP Request Id không hợp lệ";
    private static final String code503 ="Lỗi hệ thống MPS";
    private static final String code101 ="Lỗi thực hiện nghiệp vụ";
    private static final String code102 ="Lỗi khi thực hiện đăng ký dịch vụ";
    private static final String code103 ="Lỗi khi thực hiện thanh toán dịch vụ bằng tài khoản mobile";
    private static final String code104 ="Lỗi khi hủy dịch vụ";
    private static final String code201 ="Chữ ký không hợp lệ";
    private static final String code202 ="Thanh toán bị lỗi or Sai password với tài khoản MPS";
    private static final String code207 ="Đăng ký chồng gói";
    private static final String code401 ="Tài khoản không đủ thanh toán";
    private static final String code402 ="Thuê bao chưa đăng ký thanh toán";
    private static final String code403 ="Thuê bao không tồn tại";
    private static final String code404 ="Số điện thoại không hợp lệ";
    private static final String code405 ="Số điện thoại đã đổi chủ";
    private static final String code406 ="Lỗi không tìm thấy dữ liệu mobile để thanh toán";
    private static final String code407 ="CP không được phép thực hiện nghiệp vụ: tham số SUB, SER, CMD không hợp lệ/chưa được khai báo đầy đủ";
    private static final String code408 ="Thuê bao đang sử dụng dịch vụ";
    private static final String code409 ="Thuê bao bị khóa 2 chiều";
    private static final String code410 ="Số điện thoại không phải viettel";
    private static final String code411 ="Thuê bảo đã hủy dịch vụ";
    private static final String code412 ="Thuê bao không sử dụng dịch vụ";
    private static final String code413 ="Lấy giá tiền phù hợp để thanh toán bị lỗi: tham số SUB, SER, CMD, CATEGORY (với các hàm thanh toán CHARGE/DOWNLOAD) không hợp lệ/chưa được khai báo đầy đủ";
    private static final String code414 ="Thuê bao hủy dịch vụ vẫn đang nằm trong chu kỳ charge cước (thời điểm charge < thời điểm charge tiếp theo của chu kỳ)";
    private static final String code415 ="Mã OTP không hợp lệ/chưa nhập mã OTP";
    private static final String code416 ="Mã OTP không tồn tại/hết timeout cho phép";
    private static final String code417 ="Lỗi USSD time out";
    private static final String code440 ="Lỗi hệ thống";
    private static final String code501 ="Số điện thoại chưa đăng ký";
    private static final String code203 ="Không tồn tại tài khoản MPS (msisdn/password không hợp lệ)";
    private static final String code204 ="Tồn tại tài khoản MPS nhưng chưa dùng dịch vụ của CP";
    private static final String code205 ="Tồn tại tài khoản MPS và đã dùng dịch vụ của CP";
    private static final String code100 = "Giao dịch đã được xử lý";
    private static final Map<String, String> dictcode = new HashMap<>();
    
    static {
        dictcode.put("0", code0);
        dictcode.put("100", code100);
        dictcode.put("1", code1);
        dictcode.put("4", code4);    
        dictcode.put("11", code11);
        dictcode.put("13", code13);
        dictcode.put("14", code14);
        dictcode.put("15", code15);
        dictcode.put("16", code16);
        dictcode.put("17", code17);
        dictcode.put("18", code18);
        dictcode.put("22", code22);
        dictcode.put("23", code23);
        dictcode.put("24", code24);
        dictcode.put("25", code25);
        dictcode.put("101", code101);
        dictcode.put("102", code102);
        dictcode.put("103", code103);
        dictcode.put("104", code104);
        dictcode.put("201", code201);
        dictcode.put("202", code202);
        dictcode.put("207", code207);
        dictcode.put("401", code401);
        dictcode.put("402", code402);
        dictcode.put("403", code403);
        dictcode.put("405", code405);
        dictcode.put("406", code406);
        dictcode.put("407", code407);
        dictcode.put("408", code408);
        dictcode.put("404", code404);
        dictcode.put("440", code440);
        dictcode.put("410", code410);
        dictcode.put("411", code411);
        dictcode.put("412", code412);
        dictcode.put("413", code413);
        dictcode.put("414", code414);
        dictcode.put("415", code415);
        dictcode.put("416", code416);
        dictcode.put("417", code417);
        dictcode.put("503", code503);
        dictcode.put("204", code204);
        dictcode.put("205", code205);
        dictcode.put("501", code501);
        dictcode.put("203", code203);
        dictcode.put("409", code409);
    }
    
    public static String getdiscription(String code) {
        return dictcode.get(code);
    }
}
