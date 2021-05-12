package com.atguigu.thirdpatry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class demo {
    public static void main(String[] args) {
        Account acct = new Account(0);
        Customer c1 = new Customer(acct);
        Customer c2 = new Customer(acct);
        c1.setName("老婆");
        c2.setName("老公");
        c1.start();
        c2.start();
    }
}

class Account {
    private final ReentrantLock lock = new ReentrantLock();
    private double balance;

    public Account(double balance) {
        this.balance = balance;
    }

    public  void deposit(double amt) throws InterruptedException {
        lock.lock();
        if (amt > 0) {
            balance += amt;
//            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName()+"存钱成功余额" + balance);
        }
        lock.unlock();
    }
}

class Customer extends Thread {
    private Account acct;

    public Customer(Account acct) {
        this.acct = acct;
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            try {
                acct.deposit(1000);
//                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
