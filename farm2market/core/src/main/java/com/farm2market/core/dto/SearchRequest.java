package com.farm2market.core.dto;

import com.farm2market.core.enums.Category;

public class SearchRequest {
    private String query;
    private Category category;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStock;
    private String sortBy;
    private boolean ascending;
    private int limit;
    private int offset;

    public SearchRequest() {
        this.limit = 20;
        this.offset = 0;
        this.ascending = true;
    }

    public SearchRequest(String query) {
        this();
        this.query = query;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public boolean isAscending() { return ascending; }
    public void setAscending(boolean ascending) { this.ascending = ascending; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    public boolean hasFilters() {
        return category != null || minPrice != null || maxPrice != null || inStock != null;
    }
}
