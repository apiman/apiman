/// <reference path="includes.d.ts" />
declare module Core {
    /**
     * Operation arguments are stored in a map of argument name -> type
     */
    interface JMXOperationArgument {
        name: string;
        desc: string;
        type: string;
    }
    /**
     * Schema for a JMX operation object
     */
    interface JMXOperation {
        args: Array<JMXOperationArgument>;
        desc: string;
        ret: string;
        canInvoke?: boolean;
    }
    /**
     * JMX operation object that's a map of the operation name to the operation schema
     */
    interface JMXOperations {
        [methodName: string]: JMXOperation;
    }
    /**
     * JMX attribute object that contains the type, description and if it's read/write or not
     */
    interface JMXAttribute {
        desc: string;
        rw: boolean;
        type: string;
        canInvoke?: boolean;
    }
    /**
     * JMX mbean attributes, attribute name is the key
     */
    interface JMXAttributes {
        [attributeName: string]: JMXAttribute;
    }
    /**
     * JMX mbean object that contains the operations/attributes
     */
    interface JMXMBean {
        op: JMXOperations;
        attr: JMXAttributes;
        desc: string;
        canInvoke?: boolean;
    }
    /**
     * Individual JMX domain, mbean names are stored as keys
     */
    interface JMXDomain {
        [mbeanName: string]: JMXMBean;
    }
    /**
     * The top level object returned from a 'list' operation
     */
    interface JMXDomains {
        [domainName: string]: JMXDomain;
    }
    function operationToString(name: string, args: Array<JMXOperationArgument>): string;
}
