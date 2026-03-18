# Backend Convention

## JPA

### FetchType.EAGER 사용 금지

`@OneToMany`, `@ManyToOne` 등 연관관계에서 `FetchType.EAGER`를 절대 사용하지 않는다.

- EAGER는 쿼리 시점을 예측하기 어렵게 만들고, 의도치 않은 N+1을 유발한다.
- 컬렉션이 필요한 경우 `@Query`의 `JOIN FETCH` 또는 `@EntityGraph`를 명시적으로 사용한다.

```java
// ❌ 금지
@OneToMany(fetch = FetchType.EAGER)
private List<FeedbackKeyword> keywords;

// ✅ 허용 — 명시적 fetch join
@Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.keywords WHERE f.id = :id")
Optional<Feedback> findById(@Param("id") Long id);
```
