package com.ian.web.employee.otherinfoquestion;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "other_info_question")
public class OtherInfoQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String questionOneThird;
    private String questionOneFourth;
    
    private String questionTwoA;

    private String questionTwoB;
    private String questionTwoBMonth;
    private String questionTwoBDay;
    private String questionTwoBYear;
    private String questionTwoBStatusCase;

    private String questionThree;
    private String questionFour;
    private String questionFive;
    private String questionSix;

    private String questionSevenA;

    private String questionEight;
    private String questionEightType;
    private String questionEightId;
    private String questionEightValidityDate;
    private String questionEightAttachment;

    private String questionNine;

    @Transient
	private String showMode;

    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;

}
