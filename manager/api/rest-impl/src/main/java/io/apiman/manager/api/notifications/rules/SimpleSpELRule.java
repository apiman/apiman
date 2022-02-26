/*
 * The MIT License
 *
 *  Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.apiman.manager.api.notifications.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jeasy.rules.api.Action;
import org.jeasy.rules.api.Condition;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.core.BasicRule;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ParserContext;

/**
 * A {@link Rule} implementation that uses
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions">SpEL</a>
 * to evaluate and execute the rule.
 *
 * Note that this has been edited to use the safe and reduced functionality of {@link SimpleSpELCondition}
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class SimpleSpELRule extends BasicRule {

    private Condition condition = Condition.FALSE;
    private final List<Action> actions = new ArrayList<>();
    private final ParserContext parserContext;
    private BeanResolver beanResolver;

    /**
     * Create a new SpEL rule.
     */
    public SimpleSpELRule() {
        this(ParserContext.TEMPLATE_EXPRESSION);
    }

    /**
     * Create a new SpEL rule.
     *
     * @param parserContext used when parsing expressions
     */
    public SimpleSpELRule(ParserContext parserContext) {
        super(Rule.DEFAULT_NAME, Rule.DEFAULT_DESCRIPTION, Rule.DEFAULT_PRIORITY);
        this.parserContext = parserContext;
    }

    /**
     * Create a new SpEL rule.
     *
     * @param beanResolver used to resolve bean references in expressions
     */
    public SimpleSpELRule(BeanResolver beanResolver) {
        super(Rule.DEFAULT_NAME, Rule.DEFAULT_DESCRIPTION, Rule.DEFAULT_PRIORITY);
        this.parserContext = ParserContext.TEMPLATE_EXPRESSION;
        this.beanResolver = beanResolver;
    }

    /**
     * Create a new SpEL rule.
     *
     * @param parserContext used when parsing expressions
     * @param beanResolver used to resolve bean references in expressions
     */
    public SimpleSpELRule(ParserContext parserContext, BeanResolver beanResolver) {
        super(Rule.DEFAULT_NAME, Rule.DEFAULT_DESCRIPTION, Rule.DEFAULT_PRIORITY);
        this.parserContext = parserContext;
        this.beanResolver = beanResolver;
    }

    /**
     * Set rule name.
     *
     * @param name of the rule
     * @return this rule
     */
    public SimpleSpELRule name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set rule description.
     *
     * @param description of the rule
     * @return this rule
     */
    public SimpleSpELRule description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set rule priority.
     *
     * @param priority of the rule
     * @return this rule
     */
    public SimpleSpELRule priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Specify the rule's condition as SpEL expression.
     * @param condition of the rule
     * @return this rule
     */
    public SimpleSpELRule when(String condition) {
        this.condition = new SimpleSpELCondition(condition, parserContext, beanResolver);
        return this;
    }

    /**
     * Add an action specified as an SpEL expression to the rule.
     * @param action to add to the rule
     * @return this rule
     */
    public SimpleSpELRule then(String action) {
        // this.actions.add(new SpELAction(action, parserContext, beanResolver));
        throw new NoSuchElementException();
    }

    @Override
    public boolean evaluate(Facts facts) {
        return condition.evaluate(facts);
    }

    @Override
    public void execute(Facts facts) throws Exception {
        for (Action action : actions) {
            action.execute(facts);
        }
    }
}
