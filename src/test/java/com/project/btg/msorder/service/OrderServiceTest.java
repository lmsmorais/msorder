package com.project.btg.msorder.service;

import com.project.btg.msorder.entity.OrderEntity;
import com.project.btg.msorder.factory.OrderCreatedEventFactory;
import com.project.btg.msorder.factory.OrderEntityfactory;
import com.project.btg.msorder.repository.OrderRepository;
import org.bson.Document;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    OrderService orderService;

    @Captor
    ArgumentCaptor<OrderEntity> orderEntityArgumentCaptor;

    @Captor
    ArgumentCaptor<Aggregation> aggregationArgumentCaptor;

    @Nested
    class save {
        @Test
        void shouldCallrepositorySave() {
            var event = OrderCreatedEventFactory.buildWithOneItem();

            orderService.save(event);

            verify(orderRepository, times(1)).save(orderEntityArgumentCaptor.capture());
        }

        @Test
        void shouldMapEventoToEntityWithSucess() {
            var event = OrderCreatedEventFactory.buildWithOneItem();

            orderService.save(event);

            verify(orderRepository, times(1)).save(orderEntityArgumentCaptor.capture());

            var entity = orderEntityArgumentCaptor.getValue();

            assertEquals(event.codigoPedido(),  entity.getOrderId());
            assertEquals(event.codigoCliente(),  entity.getCustomerId());
            assertNotNull(entity.getTotal());
            assertEquals(event.itens().getFirst().produto(), entity.getItems().getFirst().getProduct());
            assertEquals(event.itens().getFirst().quantidade(), entity.getItems().getFirst().getQuantity());
            assertEquals(event.itens().getFirst().preco(), entity.getItems().getFirst().getPrice());
        }

        @Test
        void shouldCalculateOrderTotalWithSucess() {
            var event = OrderCreatedEventFactory.buildWithTwoItem();
            var totalItem1 = event.itens().getFirst().preco().multiply(BigDecimal.valueOf(event.itens().getFirst().quantidade()));
            var totalItem2 = event.itens().getLast().preco().multiply(BigDecimal.valueOf(event.itens().getLast().quantidade()));
            var orderTotal = totalItem1.add(totalItem2);

            orderService.save(event);

            verify(orderRepository, times(1)).save(orderEntityArgumentCaptor.capture());

            var entity = orderEntityArgumentCaptor.getValue();

            assertNotNull(entity.getTotal());
            assertEquals(orderTotal, entity.getTotal());
        }
    }

    @Nested
    class findAllByCustomerId {
        @Test
        void shouldCallRepository() {
            var customerId = 1L;
            var pageRequest = PageRequest.of(0, 10);

            doReturn(OrderEntityfactory.buildWithPage())
                    .when(orderRepository).findAllByCustomerId(customerId, pageRequest);

            var response = orderService.findAllByCustomerId(customerId, pageRequest);

            verify(orderRepository, times(1)).findAllByCustomerId(eq(customerId), eq(pageRequest));
            assertNotNull(response);
        }

        @Test
        void shouldMapResponse() {
            var customerId = 1L;
            var pageRequest = PageRequest.of(0, 10);
            var page = OrderEntityfactory.buildWithPage();

            doReturn(page)
                    .when(orderRepository).findAllByCustomerId(anyLong(), any());

            var response = orderService.findAllByCustomerId(customerId, pageRequest);

            assertEquals(page.getTotalPages(), response.getTotalPages());
            assertEquals(page.getTotalElements(), response.getTotalElements());
            assertEquals(page.getSize(), response.getSize());
            assertEquals(page.getNumber(), response.getNumber());

            assertEquals(page.getContent().getFirst().getOrderId(), response.getContent().getFirst().orderId());
            assertEquals(page.getContent().getFirst().getCustomerId(), response.getContent().getFirst().customerId());
            assertEquals(page.getContent().getFirst().getTotal(), response.getContent().getFirst().total());
        }
    }

    @Nested
    class findTotalOnOrdersByCustomerId {
        @Test
        void shouldCallMongoTemplate() {
            var customerId = 1L;
            var aggregationResult = mock(AggregationResults.class);
            var totalExpected = BigDecimal.valueOf(1);

            doReturn(new Document("total", 1)).when(aggregationResult).getUniqueMappedResult();

            doReturn(aggregationResult)
                    .when(mongoTemplate).aggregate(any(Aggregation.class), anyString(), eq(Document.class));

            var total = orderService.findTotalOrdersByCustomerId(customerId);

            verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), anyString(), eq(Document.class));
            assertEquals(totalExpected, total);
        }

        @Test
        void shouldUseCorrectAggregation() {
            var customerId = 1L;
            var aggregationResult = mock(AggregationResults.class);

            doReturn(new Document("total", 1)).when(aggregationResult).getUniqueMappedResult();

            doReturn(aggregationResult)
                    .when(mongoTemplate).aggregate(aggregationArgumentCaptor.capture(), anyString(), eq(Document.class));

            orderService.findTotalOrdersByCustomerId(customerId);

            var aggregation = aggregationArgumentCaptor.getValue();
            var aggregationExpected = newAggregation(
                    match(Criteria.where("customerId").is(customerId)),
                    group().sum("total").as("total")
            );

            assertEquals(aggregationExpected.toString(), aggregation.toString());
        }

        @Test
        void shouldUseQueryCorrectTable() {
            var customerId = 1L;
            var aggregationResult = mock(AggregationResults.class);

            doReturn(new Document("total", 1)).when(aggregationResult).getUniqueMappedResult();

            doReturn(aggregationResult)
                    .when(mongoTemplate).aggregate(aggregationArgumentCaptor.capture(), eq("tb_orders"), eq(Document.class));

            orderService.findTotalOrdersByCustomerId(customerId);

            verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq("tb_orders"), eq(Document.class));
        }
    }



}