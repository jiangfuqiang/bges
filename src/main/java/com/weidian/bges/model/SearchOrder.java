package com.weidian.bges.model;

import java.io.Serializable;

/**
 * Created by jiang on 17/11/21.
 */
public class SearchOrder implements Serializable{

    private String name;
    private Order order;

    public SearchOrder(String name, Order order) {
        this.name = name;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public static enum Order {
       DESC(1),
        ASC(0);
        private int type;
        private Order order;
        private Order(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Order getOrder() {
            return order;
        }

        public void setOrder(Order order) {
            this.order = order;
        }
    }
}

