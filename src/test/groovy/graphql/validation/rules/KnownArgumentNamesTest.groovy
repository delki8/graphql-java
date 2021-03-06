package graphql.validation.rules

import graphql.language.Argument
import graphql.language.BooleanValue
import graphql.language.StringValue
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.validation.ValidationContext
import graphql.validation.ValidationErrorCollector
import graphql.validation.ValidationErrorType
import spock.lang.Specification

import static graphql.Scalars.GraphQLBoolean
import static graphql.Scalars.GraphQLString

class KnownArgumentNamesTest extends Specification {

    ValidationContext validationContext = Mock(ValidationContext)
    ValidationErrorCollector errorCollector = new ValidationErrorCollector()
    KnownArgumentNames knownArgumentNames = new KnownArgumentNames(validationContext, errorCollector)

    def "unknown field argument"() {
        given:
        Argument argument = new Argument("unknownArg", new StringValue("value"))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString)
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLString).build()).build();
        validationContext.getFieldDef() >> fieldDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.containsValidationError(ValidationErrorType.UnknownArgument)
        errorCollector.errors.size() == 1
        errorCollector.errors[0].message == "Validation error of type UnknownArgument: Unknown field argument unknownArg"
    }

    def "known field argument"() {
        given:
        Argument argument = new Argument("knownArg", new StringValue("value"))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString)
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLString).build()).build();
        validationContext.getFieldDef() >> fieldDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.errors.isEmpty()
    }

    def "unknown directive argument"() {
        given:
        Argument argument = new Argument("unknownArg", new BooleanValue(true))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString).build()
        def directiveDefinition = GraphQLDirective.newDirective().name("directive")
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLBoolean).build()).build()
        validationContext.getFieldDef() >> fieldDefinition
        validationContext.getDirective() >> directiveDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.containsValidationError(ValidationErrorType.UnknownDirective)
        errorCollector.errors.size() == 1
        errorCollector.errors[0].message == "Validation error of type UnknownDirective: Unknown directive argument unknownArg"
    }

    def "known directive argument"() {
        given:
        Argument argument = new Argument("knownArg", new BooleanValue(true))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString).build()
        def directiveDefinition = GraphQLDirective.newDirective().name("directive")
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLBoolean).build()).build()
        validationContext.getFieldDef() >> fieldDefinition
        validationContext.getDirective() >> directiveDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.errors.isEmpty()
    }

    def "directive argument not validated against field arguments"() {
        given:
        Argument argument = new Argument("unknownArg", new BooleanValue(true))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString)
                .argument(GraphQLArgument.newArgument().name("unknownArg").type(GraphQLString).build()).build()
        def directiveDefinition = GraphQLDirective.newDirective().name("directive")
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLBoolean).build()).build()
        validationContext.getFieldDef() >> fieldDefinition
        validationContext.getDirective() >> directiveDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.containsValidationError(ValidationErrorType.UnknownDirective)
    }

    def "no error is added when directive and fieldDef from validation context are null"() {
        given:
        validationContext.directive >> null
        validationContext.fieldDef >> null
        Argument arg = new Argument("unknownArg", new BooleanValue(false))

        when:
        knownArgumentNames.checkArgument(arg)

        then:
        errorCollector.errors.isEmpty()
    }
}

