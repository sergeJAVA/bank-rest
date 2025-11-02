package com.example.bankcards.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Человек")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class XmlDto {

    @XmlElement(name = "Имя")
    private String name;

    @XmlElement(name = "Возраст")
    private int age;

}
