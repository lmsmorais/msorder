package com.project.btg.msorder.controller;

import com.project.btg.msorder.factory.OrderResponseFactory;
import com.project.btg.msorder.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    OrderController orderController;

    @Mock
    OrderService orderService;

    @Captor
    ArgumentCaptor<Long> customerIdCaptor;

    @Captor
    ArgumentCaptor<PageRequest> pageRequestCaptor;

    @Nested
    class listOrders {
        @Test
        void shouldReturnHttpOk() {
            var customerId = 1L;
            var page = 0;
            var pageSize = 10;

            doReturn(OrderResponseFactory.buildWithOneItem())
                    .when(orderService).findAllByCustomerId(anyLong(), any());

            doReturn(BigDecimal.valueOf(20.50)).when(orderService).findTotalOrdersByCustomerId(anyLong());

            var response = orderController.listOrders(customerId, page, pageSize);

            assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        }

        @Test
        void shouldPassCorrectParametersToService() {
            var customerId = 1L;
            var page = 0;
            var pageSize = 10;

            doReturn(OrderResponseFactory.buildWithOneItem())
                    .when(orderService).findAllByCustomerId(customerIdCaptor.capture(), pageRequestCaptor.capture());

            doReturn(BigDecimal.valueOf(20.50)).when(orderService).findTotalOrdersByCustomerId(customerIdCaptor.capture());

            var response = orderController.listOrders(customerId, page, pageSize);

            assertEquals(2, customerIdCaptor.getAllValues().size());
            assertEquals(customerId, customerIdCaptor.getAllValues().get(0));
            assertEquals(customerId, customerIdCaptor.getAllValues().get(1));
            assertEquals(page, pageRequestCaptor.getValue().getPageNumber());
            assertEquals(pageSize, pageRequestCaptor.getValue().getPageSize());
        }

        @Test
        void shouldReturnResponseBodyCorrectly() {
            var customerId = 1L;
            var page = 0;
            var pageSize = 10;
            var totalOnOrders = BigDecimal.valueOf(20.50);
            var pagination = OrderResponseFactory.buildWithOneItem();

            doReturn(pagination)
                    .when(orderService).findAllByCustomerId(anyLong(), any());

            doReturn(BigDecimal.valueOf(20.50)).when(orderService).findTotalOrdersByCustomerId(anyLong());

            var response = orderController.listOrders(customerId, page, pageSize);

            assertNotNull(response);
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertNotNull(response.getBody().paginationResponse());
            assertNotNull(response.getBody().summary());

            assertEquals(totalOnOrders, response.getBody().summary().get("totalOnOrders"));

            assertEquals(pagination.getTotalElements(), response.getBody().paginationResponse().totalElements());
            assertEquals(pagination.getTotalPages(), response.getBody().paginationResponse().totalPages());
            assertEquals(pagination.getNumber(), response.getBody().paginationResponse().page());
            assertEquals(pagination.getSize(), response.getBody().paginationResponse().pageSize());

            assertEquals(pagination.getContent(), response.getBody().data());
        }
    }

}