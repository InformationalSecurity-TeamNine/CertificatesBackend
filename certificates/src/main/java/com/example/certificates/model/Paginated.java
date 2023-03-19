package com.example.certificates.model;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Paginated<T> {
    private int totalCount;
    private Set<T> results;
}
