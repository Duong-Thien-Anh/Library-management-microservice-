package com.fptdemo.borrowservice.repository;

import com.fptdemo.borrowservice.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    /** Find all borrow records for a specific user */
    List<BorrowRecord> findByUsername(String username);

    /** Find all active borrows for a specific book */
    List<BorrowRecord> findByBookIdAndStatus(Long bookId, BorrowRecord.BorrowStatus status);

    /** Check if user already has this book borrowed */
    boolean existsByUsernameAndBookIdAndStatus(String username, Long bookId, BorrowRecord.BorrowStatus status);
}

