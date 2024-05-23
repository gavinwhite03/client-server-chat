/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client_server_assesment.server;

/**
 *
 * @author anoth
 */
import static spark.Spark.*;

public class SparkTest {
    public static void main(String[] args) {
        get("/hello", (req, res) -> {
            res.type("text/html");
            return "<html><body><h1>Hello, Spark Java!</h1></body></html>";
        });

        awaitInitialization();
    }
}


