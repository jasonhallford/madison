package io.miscellanea.madison.api;

import org.jetbrains.annotations.NotNull;

public record Author(@NotNull String firstName, String middleName, @NotNull String lastName, String suffix) {
    public Author(String firstName, String lastName){
        this(firstName,null,lastName,null);
    }

    public static Author fromString(String fullName){
        return null;
    }

    public String fullName(){
        var builder = new StringBuilder();

        builder.append(this.firstName).append(" ");
        if(!this.middleName.isEmpty()){
            builder.append(this.middleName).append(" ");
        }
        builder.append(this.lastName);
        if(!this.suffix.isEmpty()){
            builder.append(", ").append(this.suffix);
        }

        return builder.toString();
    }
}
