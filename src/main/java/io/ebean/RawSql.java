package io.ebean;

/**
 * Used to build object graphs based on a raw SQL statement (rather than
 * generated by Ebean).
 * <p>
 * If you don't want to build object graphs you can use {@link SqlQuery} instead
 * which returns {@link SqlRow} objects rather than entity beans.
 * <p>
 * <b>Unparsed RawSql:</b>
 * <p>
 * When RawSql is created via {@link RawSqlBuilder#unparsed(String)} then Ebean can not
 * modify the SQL at all. It can't add any extra expressions into the SQL.
 * <p>
 * <b>Parsed RawSql:</b>
 * <p>
 * When RawSql is created via {@link RawSqlBuilder#parse(String)} then Ebean will parse the
 * SQL and find places in the SQL where it can add extra where expressions, add
 * extra having expressions or replace the order by clause. If you want to
 * explicitly tell Ebean where these insertion points are you can place special
 * strings into your SQL ({@code ${where}} or {@code ${andWhere}} and {@code ${having}} or
 * {@code ${andHaving})}.
 * <p>
 * If the SQL already includes a WHERE clause put in {@code ${andWhere}} in the location
 * you want Ebean to add any extra where expressions. If the SQL doesn't have a
 * WHERE clause put {@code ${where}} in instead. Similarly you can put in {@code ${having}} or
 * {@code ${andHaving}} where you want Ebean put add extra having expressions.
 * <p>
 * <b>Aggregates:</b>
 * <p>
 * Often RawSql will be used with Aggregate functions (sum, avg, max etc). The
 * follow example shows an example based on Total Order Amount -
 * sum(d.order_qty*d.unit_price).
 * </p>
 * <p>
 * We can use a OrderAggregate bean that has a &#064;Sql to indicate it is based
 * on RawSql and not based on a real DB Table or DB View. It has some properties
 * to hold the values for the aggregate functions (sum etc) and a &#064;OneToOne
 * to Order.
 * <p>
 * <h3>Example OrderAggregate</h3>
 * <pre>{@code
 *  ...
 *   // ＠Sql indicates to that this bean
 *   // is based on RawSql rather than a table
 *
 *   ＠Entity
 *   ＠Sql
 *   public class OrderAggregate {
 *
 *    ＠OneToOne
 *    Order order;
 *
 *    Double totalAmount;
 *
 *    Double totalItems;
 *
 *    // getters and setters
 *    ...
 *   }
 * }</pre>
 *
 * <h3>Example 1:</h3>
 *
 * <pre>{@code
 *
 *   String sql = " select order_id, o.status, c.id, c.name, sum(l.order_qty*l.unit_price) as totalAmount"
 *     + " from order o"
 *     + " join customer c on c.id = o.customer_id "
 *     + " join order_line l on l.order_id = o.id " + " group by order_id, o.status ";
 *
 *   RawSql rawSql = RawSqlBuilder.parse(sql)
 *     // map the sql result columns to bean properties
 *     .columnMapping("order_id", "order.id")
 *     .columnMapping("o.status", "order.status")
 *     .columnMapping("c.id", "order.customer.id")
 *     .columnMapping("c.name", "order.customer.name")
 *     // we don't need to map this one due to the sql column alias
 *     // .columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
 *     .create();
 *
 *   List<OrderAggregate> list = DB.find(OrderAggregate.class)
 *       .setRawSql(rawSql)
 *       .where().gt("order.id", 0)
 *       .having().gt("totalAmount", 20)
 *       .findList();
 *
 *
 * }</pre>
 *
 * <h3>Example 2:</h3>
 * <p>
 * The following example uses a FetchConfig().query() so that after the initial
 * RawSql query is executed Ebean executes a secondary query to fetch the
 * associated order status, orderDate along with the customer name.
 *
 * <pre>{@code
 *
 *  String sql = " select order_id, 'ignoreMe', sum(l.order_qty*l.unit_price) as totalAmount "
 *     + " from order_line l"
 *     + " group by order_id ";
 *
 *   RawSql rawSql = RawSqlBuilder.parse(sql)
 *     .columnMapping("order_id", "order.id")
 *     .columnMappingIgnore("'ignoreMe'")
 *     .create();
 *
 *   List<OrderAggregate> orders = DB.find(OrderAggregate.class)
 *     .setRawSql(rawSql)
 *     .fetch("order", "status,orderDate", new FetchConfig().query())
 *     .fetch("order.customer", "name")
 *     .where().gt("order.id", 0)
 *     .having().gt("totalAmount", 20)
 *     .order().desc("totalAmount")
 *     .setMaxRows(10)
 *     .findList();
 *
 * }</pre>
 * <h3>Example 3: tableAliasMapping</h3>
 * <p>
 * Instead of mapping each column you can map each table alias to a path using tableAliasMapping().
 * <pre>{@code
 *
 *   String rs = "select o.id, o.status, c.id, c.name, "+
 *               " l.id, l.order_qty, p.id, p.name " +
 *               "from orders o join o_customer c on c.id = o.customer_id " +
 *               "join order_line l on l.order_id = o.id  " +
 *               "join product p on p.id = l.product_id  " +
 *               "where o.id <= :maxOrderId  and p.id = :productId "+
 *               "order by o.id, l.id asc";
 *
 *  RawSql rawSql = RawSqlBuilder.parse(rs)
 *       .tableAliasMapping("c", "customer")
 *       .tableAliasMapping("l", "lines")
 *       .tableAliasMapping("p", "lines.product")
 *       .create();
 *
 *  List<Order> ordersFromRaw = DB.find(Order.class)
 *       .setRawSql(rawSql)
 *       .setParameter("maxOrderId", 2)
 *       .setParameter("productId", 1)
 *       .findList();
 *
 * }</pre>
 * <p>
 * Note that lazy loading also works with object graphs built with RawSql.
 */
public interface RawSql {

}
