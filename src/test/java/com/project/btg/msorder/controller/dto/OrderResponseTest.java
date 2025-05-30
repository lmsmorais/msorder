package com.project.btg.msorder.controller.dto;

import com.project.btg.msorder.factory.OrderEntityfactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderResponseTest {

    @Nested
    class FromEntity {

        @Test
        void shouldMapCorrectly() {
            var input = OrderEntityfactory.build();

            var output = OrderResponse.fromEntity(input);

            assertEquals(input.getOrderId(), output.orderId());
            assertEquals(input.getCustomerId(), output.customerId());
            assertEquals(input.getTotal(), output.total());
        }
    }

}