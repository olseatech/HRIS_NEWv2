package com.ian.web.employee;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data public class FlexDatalistResult {
	Set<Employee> results;
}
