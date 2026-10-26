#!/bin/bash
# Test création objectif

curl -X POST http://localhost:8080/api/objectives \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "line": "Ligne A",
    "periodType": "MONTHLY",
    "startDate": "2026-08-09",
    "endDate": "2026-08-31",
    "targetQuantity": 2345,
    "targetQualityRate": 95.0,
    "maxDefects": 5,
    "active": true,
    "notes": "Test objectif"
  }'
