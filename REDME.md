# Project msorder

### Docker step:
docker compose up

### RabbitMq access:
http://localhost:15672
user: guest
password: guest

### Expected consumer json:

```json
{
   "codigoPedido": 1001,
   "codigoCliente":1,
   "itens": [
       {
           "produto": "lápis",
           "quantidade": 100,
           "preco": 1.10
       },
       {
           "produto": "caderno",
           "quantidade": 10,
           "preco": 1.00
       }
   ]
}
```
