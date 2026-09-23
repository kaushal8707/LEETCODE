package ai.ml.leetcode.completablefuture.practiceset_1;

import ai.ml.leetcode.completablefuture.database.EmployeeDataBase;
import ai.ml.leetcode.completablefuture.model.Employee;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** Employee Reminder Service Application **/

public class PracticeSet__1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> voidCompletableFuture = sendReminderToAllEmployee();
        System.out.println(voidCompletableFuture.get());
    }

    private static CompletableFuture<Void> sendReminderToAllEmployee() throws ExecutionException, InterruptedException {
        Executor executor = Executors.newFixedThreadPool(5);
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("fetch employee records : " + Thread.currentThread().getName());
            List<Employee> emp = EmployeeDataBase.fetchEmployee();
            return emp;
        }, executor).thenApplyAsync(employees -> {
            System.out.println("filter is this employee a new joinee" + Thread.currentThread().getName());
            return employees.stream().filter(employee -> "TRUE".equals(employee.getNewJoiner())).collect(Collectors.toList());
        }, executor).thenApplyAsync(employees -> {
            System.out.println("Is Training not completed yet ? " + Thread.currentThread().getName());
            return employees.stream().filter(emp -> "TRUE".equals(emp.getLearningPending())).collect(Collectors.toList());
        }, executor).thenApplyAsync(employees -> {
            System.out.println("collect mailId of each training painding new joinee employees" + Thread.currentThread().getName());
            return employees.stream().map(Employee::getEmail).collect(Collectors.toList());
        }, executor).thenAcceptAsync(mails -> {
            System.out.println("collect mail Id and start sending mail to complete training asap !!!");
            mails.forEach(PracticeSet__1::startSendingMail);
        });
        return voidCompletableFuture;
    }

    private static void startSendingMail(String mail) {
        System.out.println("sending mail to : "+mail);
    }
}
