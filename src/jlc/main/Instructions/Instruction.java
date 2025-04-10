package jlc.main.Instructions;

// Intruction interface that allowes us to generate a string of intrusctions
public interface Instruction {

    // Since we want to add space to make our generated code beutiful
    void AddNumOfSpaceForPrefix(int num);

    // This will generate intruction's string that we need to put in our .ll file.
    String GenerateInstruction();
}
