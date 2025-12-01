package com.sernova.web;

import com.sernova.domain.Address;
import com.sernova.domain.Person;
import com.sernova.domain.PersonRepository;
import com.sernova.dto.AddressDto;
import com.sernova.dto.PersonDto;
import com.sernova.dto.PersonWithAddressesDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final PersonRepository personRepository;

    public ApiController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from Sernova test API");
    }

    @GetMapping("/persons")
    public ResponseEntity<List<PersonDto>> getPersons() {
        List<Person> persons = personRepository.findAll();
        List<PersonDto> result = persons.stream()
                .map(p -> new PersonDto(
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName()))
                .toList();   // returns List<PersonDto>
        return ResponseEntity.ok(result);
    }

    @GetMapping("/persons-with-addresses")
    public ResponseEntity<List<PersonWithAddressesDto>> personsWithAddresses() {
        List<Person> persons = personRepository.findAllWithAddresses(); // single join query

        List<PersonWithAddressesDto> result = persons.stream()
                .map(p -> new PersonWithAddressesDto(
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getAddresses().stream()
                                .map(a -> new AddressDto(
                                        a.getId(),
                                        a.getLine1(),
                                        a.getCity(),
                                        a.getCountry(),
                                        a.getType()
                                ))
                                .toList()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }


    @PostMapping("/seed/people")
    public ResponseEntity<String> seedPeople() {
        // Insert 10,000 persons with 2 addresses each. Simple batching to reduce memory usage.
        int total = 10_000;
        int batchSize = 500;
        List<Person> batch = new ArrayList<>(batchSize);
        for (int i = 1; i <= total; i++) {
            Person p = new Person();
            p.setFirstName("First" + i);
            p.setLastName("Last" + i);

            Address home = new Address();
            home.setLine1("Home Street " + i);
            home.setCity("City" + (i % 100));
            home.setCountry("Country");
            home.setType("HOME");
            p.addAddress(home);

            Address work = new Address();
            work.setLine1("Work Ave " + i);
            work.setCity("City" + (i % 100));
            work.setCountry("Country");
            work.setType("WORK");
            p.addAddress(work);

            batch.add(p);

            if (batch.size() == batchSize) {
                personRepository.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            personRepository.saveAll(batch);
        }
        return ResponseEntity.ok("Seeded " + total + " persons with 2 addresses each");
    }
}
