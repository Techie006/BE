package com.sparta.cookbank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientService {


    public ResponseEntity<?> findIngredient() {

        return ResponseEntity.ok().body("엄");
    }

    public ResponseEntity<?> enterIngredient() {

        return ResponseEntity.ok().body("엄");
    }
}
