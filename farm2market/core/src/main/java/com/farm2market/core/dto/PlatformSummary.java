package com.farm2market.core.dto;

public class PlatformSummary {
    private long totalUsers;
    private long totalCustomers;
    private long totalFarmers;
    private long totalAdmins;
    private long activeListings;
    private long totalOrders;
    private long ordersToday;
    private long ordersThisWeek;
    private long ordersThisMonth;
    private long pendingOrders;
    private double revenueToday;
    private double revenueThisWeek;
    private double revenueThisMonth;
    private long openReports;

    public PlatformSummary() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getTotalFarmers() { return totalFarmers; }
    public void setTotalFarmers(long totalFarmers) { this.totalFarmers = totalFarmers; }
    public long getTotalAdmins() { return totalAdmins; }
    public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }
    public long getActiveListings() { return activeListings; }
    public void setActiveListings(long activeListings) { this.activeListings = activeListings; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public long getOrdersToday() { return ordersToday; }
    public void setOrdersToday(long ordersToday) { this.ordersToday = ordersToday; }
    public long getOrdersThisWeek() { return ordersThisWeek; }
    public void setOrdersThisWeek(long ordersThisWeek) { this.ordersThisWeek = ordersThisWeek; }
    public long getOrdersThisMonth() { return ordersThisMonth; }
    public void setOrdersThisMonth(long ordersThisMonth) { this.ordersThisMonth = ordersThisMonth; }
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
    public double getRevenueToday() { return revenueToday; }
    public void setRevenueToday(double revenueToday) { this.revenueToday = revenueToday; }
    public double getRevenueThisWeek() { return revenueThisWeek; }
    public void setRevenueThisWeek(double revenueThisWeek) { this.revenueThisWeek = revenueThisWeek; }
    public double getRevenueThisMonth() { return revenueThisMonth; }
    public void setRevenueThisMonth(double revenueThisMonth) { this.revenueThisMonth = revenueThisMonth; }
    public long getOpenReports() { return openReports; }
    public void setOpenReports(long openReports) { this.openReports = openReports; }
}
