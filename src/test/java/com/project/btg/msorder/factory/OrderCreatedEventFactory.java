package com.project.btg.msorder.factory;

import com.project.btg.msorder.dto.OrderCreatedEvent;
import com.project.btg.msorder.dto.OrderItemEvent;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreatedEventFactory {

    public static OrderCreatedEvent buildWithOneItem() {
        var itens = new OrderItemEvent("notebook", 1, BigDecimal.valueOf(20.50));

        return new OrderCreatedEvent(1L, 2L, List.of(itens));
    }

    public static OrderCreatedEvent buildWithTwoItem() {
        var item1 = new OrderItemEvent("notebook", 1, BigDecimal.valueOf(20.50));
        var item2 = new OrderItemEvent("notebook", 1, BigDecimal.valueOf(30.60));

        return new OrderCreatedEvent(1L, 2L, List.of(item1, item2));
    }
}
