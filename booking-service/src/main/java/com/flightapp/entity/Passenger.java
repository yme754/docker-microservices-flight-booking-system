package com.flightapp.entity;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Passenger {
	private String id;
	@NotBlank(message = "Passenger name is required")
	private String name;
	@NotBlank(message = "Gender is required of type (Male/Female)")
	private String gender;
	@Positive(message = "Age must be > 0")
	@Max(value = 90, message = "Age must be < 90")
	private int age;
	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;
	private String bookingId;
}
