//package org.logitrack.utils;
//
//import lombok.RequiredArgsConstructor;
//import org.logitrack.repository.OrderRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@RequiredArgsConstructor
//@Service
//public class OrderReferenceService {
//    OrderRepository orderRepository;
//    public String generateOrderReference() {
//        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        int maxSequentialNumber = orderRepository.findMaxSequentialNumberForDate(datePart);
//        int nextSequentialNumber = maxSequentialNumber + 1;
//        String reference = "ORD-" + datePart + "-" + String.format("%03d", nextSequentialNumber);
//        return reference;
//    }
//
//}
