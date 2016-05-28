/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.jndi.internal.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.jndi.internal.Constants;
import org.wso2.carbon.jndi.internal.util.NameParserImpl;
import org.wso2.carbon.jndi.internal.util.StringManager;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.NamingManager;

/**
 * JNDI Context implementation.
 */
public class NamingContext implements Context {

    private static final Logger logger = LoggerFactory.getLogger(NamingContext.class);

    /**
     * Name parser for this context.
     */
    protected static final NameParser nameParser = new NameParserImpl();

    /**
     * Environment.
     */
    protected final Hashtable<String, Object> env;

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(Constants.PACKAGE);

    /**
     * Bindings in this Context.
     */
    protected final HashMap<String, NamingEntry> bindings;

    /**
     * Name of the associated with JNDI Context.
     */
    protected final String name;

    /**
     * Builds a jndi context using the given environment.
     *
     * @param environment
     * @param name
     * @throws NamingException
     */
    public NamingContext(Hashtable<String, Object> environment, String name) throws NamingException {
        this(environment, name, new HashMap<>());
    }

    /**
     * Builds a jndi context using the given environment.
     *
     * @param environment
     * @param name
     * @param bindings
     * @throws NamingException
     */
    public NamingContext(Hashtable<String, Object> environment,
                         String name,
                         HashMap<String, NamingEntry> bindings) throws NamingException {

        this.env = new Hashtable<>();
        this.name = name;
        // Populating the environment hashtable
        if (environment != null) {
            Enumeration<String> envEntries = environment.keys();
            while (envEntries.hasMoreElements()) {
                String entryName = envEntries.nextElement();
                addToEnvironment(entryName, environment.get(entryName));
            }
        }
        this.bindings = bindings;
    }

    /**
     * Retrieves the named object. If name is empty, returns a new instance
     * of this context (which represents the same jndi context as this
     * context, but its environment may be modified independently and it may
     * be accessed concurrently).
     *
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name, true);
    }

    /**
     * Retrieves the named object.
     *
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name), true);
    }


    /**
     * Binds a name to an object. All intermediate contexts and the target
     * context (that named by all but terminal atomic component of the name)
     * must already exist.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws NameAlreadyBoundException                         if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if object
     *                                                           did not supply all mandatory attributes
     * @throws NamingException                                   if a jndi exception is encountered
     */
    @Override
    public void bind(Name name, Object obj) throws NamingException {
        bind(name, obj, false);
    }


    /**
     * Binds a name to an object.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws NameAlreadyBoundException                         if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if object
     *                                                           did not supply all mandatory attributes
     * @throws NamingException                                   if a jndi exception is encountered
     */
    @Override
    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }


    /**
     * Binds a name to an object, overwriting any existing binding. All
     * intermediate contexts and the target context (that named by all but
     * terminal atomic component of the name) must already exist.
     * <p>
     * If the object is a DirContext, any existing attributes associated with
     * the name are replaced with those of the object. Otherwise, any
     * existing attributes associated with the name remain unchanged.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws javax.naming.directory.InvalidAttributesException if object
     *                                                           did not supply all mandatory attributes
     * @throws NamingException                                   if a jndi exception is encountered
     */
    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        bind(name, obj, true);
    }


    /**
     * Binds a name to an object, overwriting any existing binding.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws javax.naming.directory.InvalidAttributesException if object
     *                                                           did not supply all mandatory attributes
     * @throws NamingException                                   if a jndi exception is encountered
     */
    @Override
    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }


    /**
     * Unbinds the named object. Removes the terminal atomic name in name
     * from the target context--that named by all but the terminal atomic
     * part of name.
     * <p>
     * This method is idempotent. It succeeds even if the terminal atomic
     * name is not bound in the target context, but throws
     * NameNotFoundException if any of the intermediate contexts do not exist.
     *
     * @param name the name to bind; may not be empty
     * @throws NameNotFoundException if an intermediate context does not
     *                               exist
     * @throws NamingException       if a jndi exception is encountered
     */
    @Override
    public void unbind(Name name) throws NamingException {

        while ((!name.isEmpty()) && (name.get(0).length() == 0))
            name = name.getSuffix(1);
        if (name.isEmpty())
            throw new NamingException(sm.getString("namingContext.invalidName"));

        NamingEntry entry = bindings.get(name.get(0));

        if (entry == null) {
            throw new NameNotFoundException(sm.getString("namingContext.nameNotBound", name, name.get(0)));
        }

        if (name.size() > 1) {
            if (entry.type == NamingEntry.CONTEXT) {
                ((Context) entry.value).unbind(name.getSuffix(1));
            } else {
                throw new NamingException(sm.getString("namingContext.contextExpected"));
            }
        } else {
            bindings.remove(name.get(0));
        }

    }


    /**
     * Unbinds the named object.
     *
     * @param name the name to bind; may not be empty
     * @throws NameNotFoundException if an intermediate context does not
     *                               exist
     * @throws NamingException       if a jndi exception is encountered
     */
    @Override
    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }


    /**
     * Binds a new name to the object bound to an old name, and unbinds the
     * old name. Both names are relative to this context. Any attributes
     * associated with the old name become associated with the new name.
     * Intermediate contexts of the old name are not changed.
     *
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NameAlreadyBoundException if newName is already bound
     * @throws NamingException           if a jndi exception is encountered
     */
    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        Object value = lookup(oldName);
        bind(newName, value);
        unbind(oldName);
    }


    /**
     * Binds a new name to the object bound to an old name, and unbinds the
     * old name.
     *
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NameAlreadyBoundException if newName is already bound
     * @throws NamingException           if a jndi exception is encountered
     */
    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(new CompositeName(oldName), new CompositeName(newName));
    }


    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. The contents of any subcontexts are
     * not included.
     * <p>
     * If a binding is added to or removed from this context, its effect on
     * an enumeration previously returned is undefined.
     *
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     * this context. Each element of the enumeration is of type NameClassPair.
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        // Removing empty parts
        while ((!name.isEmpty()) && (name.get(0).length() == 0)) {
            name = name.getSuffix(1);
        }

        if (name.isEmpty()) {
            return new NamingContextEnumeration(bindings.values().iterator());
        }

        NamingEntry entry = bindings.get(name.get(0));

        if (entry == null) {
            throw new NameNotFoundException(sm.getString("namingContext.nameNotBound", name, name.get(0)));
        }

        if (entry.type != NamingEntry.CONTEXT) {
            throw new NamingException(sm.getString("namingContext.contextExpected"));
        }
        return ((Context) entry.value).list(name.getSuffix(1));
    }


    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them.
     *
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     * this context. Each element of the enumeration is of type NameClassPair.
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return list(new CompositeName(name));
    }


    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them. The contents of any subcontexts are not
     * included.
     * <p>
     * If a binding is added to or removed from this context, its effect on
     * an enumeration previously returned is undefined.
     *
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context.
     * Each element of the enumeration is of type Binding.
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {

        // Removing empty parts
        while ((!name.isEmpty()) && (name.get(0).length() == 0)) {
            name = name.getSuffix(1);
        }

        if (name.isEmpty()) {
            return new NamingContextBindingsEnumeration(bindings.values().iterator(), this);
        }

        NamingEntry entry = bindings.get(name.get(0));

        if (entry == null) {
            throw new NameNotFoundException(sm.getString("namingContext.nameNotBound", name, name.get(0)));
        }

        if (entry.type != NamingEntry.CONTEXT) {
            throw new NamingException(sm.getString("namingContext.contextExpected"));
        }
        return ((Context) entry.value).listBindings(name.getSuffix(1));
    }


    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them.
     *
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context.
     * Each element of the enumeration is of type Binding.
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }


    /**
     * Destroys the named context and removes it from the namespace. Any
     * attributes associated with the name are also removed. Intermediate
     * contexts are not destroyed.
     * <p>
     * This method is idempotent. It succeeds even if the terminal atomic
     * name is not bound in the target context, but throws
     * NameNotFoundException if any of the intermediate contexts do not exist.
     * <p>
     * In a federated jndi system, a context from one jndi system may be
     * bound to a name in another. One can subsequently look up and perform
     * operations on the foreign context using a composite name. However, an
     * attempt destroy the context using this composite name will fail with
     * NotContextException, because the foreign context is not a "subcontext"
     * of the context in which it is bound. Instead, use unbind() to remove
     * the binding of the foreign context. Destroying the foreign context
     * requires that the destroySubcontext() be performed on a context from
     * the foreign context's "native" jndi system.
     *
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NameNotFoundException if an intermediate context does not
     *                               exist
     * @throws NotContextException   if the name is bound but does not name
     *                               a context, or does not name a context of the appropriate type
     */
    @Override
    public void destroySubcontext(Name name) throws NamingException {

        while ((!name.isEmpty()) && (name.get(0).length() == 0))
            name = name.getSuffix(1);
        if (name.isEmpty())
            throw new NamingException(sm.getString("namingContext.invalidName"));

        NamingEntry entry = bindings.get(name.get(0));

        if (entry == null) {
            throw new NameNotFoundException(sm.getString("namingContext.nameNotBound", name, name.get(0)));
        }

        if (name.size() > 1) {
            if (entry.type == NamingEntry.CONTEXT) {
                ((Context) entry.value).destroySubcontext(name.getSuffix(1));
            } else {
                throw new NamingException(sm.getString("namingContext.contextExpected"));
            }
        } else {
            if (entry.type == NamingEntry.CONTEXT) {
                ((Context) entry.value).close();
                bindings.remove(name.get(0));
            } else {
                throw new NotContextException(sm.getString("namingContext.contextExpected"));
            }
        }

    }


    /**
     * Destroys the named context and removes it from the namespace.
     *
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NameNotFoundException if an intermediate context does not
     *                               exist
     * @throws NotContextException   if the name is bound but does not name
     *                               a context, or does not name a context of the appropriate type
     */
    @Override
    public void destroySubcontext(String name)
            throws NamingException {
        destroySubcontext(new CompositeName(name));
    }


    /**
     * Creates and binds a new context. Creates a new context with the given
     * name and binds it in the target context (that named by all but
     * terminal atomic component of the name). All intermediate contexts and
     * the target context must already exist.
     *
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NameAlreadyBoundException                         if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if creation
     *                                                           of the sub-context requires specification of mandatory attributes
     * @throws NamingException                                   if a jndi exception is encountered
     */
    @Override
    public Context createSubcontext(Name name) throws NamingException {
        NamingContext newContext = new NamingContext(env, this.name);
        bind(name, newContext);
        return newContext;
    }


    /**
     * Creates and binds a new context.
     *
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NameAlreadyBoundException                         if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if creation
     *                                                           of the sub-context requires specification of mandatory attributes
     * @throws NamingException                                   if a jndi exception is encountered
     */
    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompositeName(name));
    }


    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name. If the object bound to name is not a
     * link, returns the object itself.
     *
     * @param name the name of the object to look up
     * @return the object bound to name, not following the terminal link
     * (if any).
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name, false);
    }


    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name.
     *
     * @param name the name of the object to look up
     * @return the object bound to name, not following the terminal link
     * (if any).
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookup(new CompositeName(name), false);
    }


    /**
     * Retrieves the parser associated with the named context. In a
     * federation of namespaces, different jndi systems will parse names
     * differently. This method allows an application to get a parser for
     * parsing names into their atomic components using the jndi convention
     * of a particular jndi system. Within any single jndi system,
     * NameParser objects returned by this method must be equal (using the
     * equals() test).
     *
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     * components
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public NameParser getNameParser(Name name) throws NamingException {

        while ((!name.isEmpty()) && (name.get(0).length() == 0))
            name = name.getSuffix(1);
        if (name.isEmpty())
            return nameParser;

        if (name.size() > 1) {
            Object obj = bindings.get(name.get(0));
            if (obj instanceof Context) {
                return ((Context) obj).getNameParser(name.getSuffix(1));
            } else {
                throw new NotContextException(sm.getString("namingContext.contextExpected"));
            }
        }

        return nameParser;

    }


    /**
     * Retrieves the parser associated with the named context.
     *
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     * components
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(new CompositeName(name));
    }


    /**
     * Composes the name of this context with a name relative to this context.
     * <p>
     * Given a name (name) relative to this context, and the name (prefix)
     * of this context relative to one of its ancestors, this method returns
     * the composition of the two names using the syntax appropriate for the
     * jndi system(s) involved. That is, if name names an object relative
     * to this context, the result is the name of the same object, but
     * relative to the ancestor context. None of the names may be null.
     *
     * @param name   a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        prefix = (Name) prefix.clone();
        return prefix.addAll(name);
    }


    /**
     * Composes the name of this context with a name relative to this context.
     *
     * @param name   a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return prefix + "/" + name;
    }


    /**
     * Adds a new environment property to the environment of this context. If
     * the property already exists, its value is overwritten.
     *
     * @param propName the name of the environment property to add; may not
     *                 be null
     * @param propVal  the value of the property to add; may not be null
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return env.put(propName, propVal);
    }


    /**
     * Removes an environment property from the environment of this context.
     *
     * @param propName the name of the environment property to remove;
     *                 may not be null
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return env.remove(propName);
    }


    /**
     * Retrieves the environment in effect for this context. See class
     * description for more details on environment properties.
     * The caller should not make any changes to the object returned: their
     * effect on the context is undefined. The environment of this context
     * may be changed using addToEnvironment() and removeFromEnvironment().
     *
     * @return the environment of this context; never null
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return env;
    }


    /**
     * Closes this context. This method releases this context's resources
     * immediately, instead of waiting for them to be released automatically
     * by the garbage collector.
     * This method is idempotent: invoking it on a context that has already
     * been closed has no effect. Invoking any other method on a closed
     * context is not allowed, and results in undefined behaviour.
     *
     * @throws NamingException if a jndi exception is encountered
     */
    @Override
    public void close() throws NamingException {
        env.clear();
    }


    /**
     * Retrieves the full name of this context within its own namespace.
     * <p>
     * Many jndi services have a notion of a "full name" for objects in
     * their respective namespaces. For example, an LDAP entry has a
     * distinguished name, and a DNS record has a fully qualified name. This
     * method allows the client application to retrieve this name. The string
     * returned by this method is not a JNDI composite name and should not be
     * passed directly to context methods. In jndi systems for which the
     * notion of full name does not make sense,
     * OperationNotSupportedException is thrown.
     *
     * @return this context's name in its own namespace; never null
     * @throws OperationNotSupportedException if the jndi system does
     *                                        not have the notion of a full name
     * @throws NamingException                if a jndi exception is encountered
     */
    @Override
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException(sm.getString("namingContext.noAbsoluteName"));
    }


    /**
     * Retrieves the named object.
     *
     * @param name         the name of the object to look up
     * @param resolveLinks If true, the links will be resolved
     * @return the object bound to name
     * @throws NamingException if a jndi exception is encountered
     */
    protected Object lookup(Name name, boolean resolveLinks) throws NamingException {

        // Removing empty parts
        while ((!name.isEmpty()) && (name.get(0).length() == 0))
            name = name.getSuffix(1);
        if (name.isEmpty()) {
            // If name is empty, a newly allocated jndi context is returned
            return new NamingContext(env, this.name, bindings);
        }

        NamingEntry entry = bindings.get(name.get(0));

        if (entry == null) {
            throw new NameNotFoundException(sm.getString("namingContext.nameNotBound", name, name.get(0)));
        }

        if (name.size() > 1) {
            // If the size of the name is greater that 1, then we go through a
            // number of subcontexts.
            if (entry.type != NamingEntry.CONTEXT) {
                throw new NamingException(sm.getString("namingContext.contextExpected"));
            }
            return ((Context) entry.value).lookup(name.getSuffix(1));
        } else {
            if ((resolveLinks) && (entry.type == NamingEntry.LINK_REF)) {
                String link = ((LinkRef) entry.value).getLinkName();
                if (link.startsWith(".")) {
                    // Link relative to this context
                    return lookup(link.substring(1));
                } else {
                    return (new InitialContext(env)).lookup(link);
                }
            } else if (entry.type == NamingEntry.REFERENCE) {
                try {
                    return NamingManager.getObjectInstance
                            (entry.value, name, this, env);
                } catch (NamingException e) {
                    throw e;
                } catch (Exception e) {
//                    log.warn(sm.getString
//                             ("namingContext.failResolvingReference"), e);
                    throw new NamingException(e.getMessage());
                }
            } else {
                return entry.value;
            }
        }

    }


    /**
     * Binds a name to an object. All intermediate contexts and the target
     * context (that named by all but terminal atomic component of the name)
     * must already exist.
     *
     * @param name   the name to bind; may not be empty
     * @param obj    the object to bind; possibly null
     * @param rebind if true, then perform a rebind (ie, overwrite)
     * @throws NameAlreadyBoundException                         if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if object
     *                                                           did not supply all mandatory attributes
     * @throws NamingException                                   if a jndi exception is encountered
     */
    protected void bind(Name name, Object obj, boolean rebind) throws NamingException {

        while ((!name.isEmpty()) && (name.get(0).length() == 0))
            name = name.getSuffix(1);
        if (name.isEmpty())
            throw new NamingException(sm.getString("namingContext.invalidName"));

        NamingEntry entry = bindings.get(name.get(0));

        if (name.size() > 1) {
            if (entry == null) {
                throw new NameNotFoundException(sm.getString("namingContext.nameNotBound", name, name.get(0)));
            }
            if (entry.type == NamingEntry.CONTEXT) {
                if (rebind) {
                    ((Context) entry.value).rebind(name.getSuffix(1), obj);
                } else {
                    ((Context) entry.value).bind(name.getSuffix(1), obj);
                }
            } else {
                throw new NamingException(sm.getString("namingContext.contextExpected"));
            }
        } else {
            if ((!rebind) && (entry != null)) {
                throw new NameAlreadyBoundException(sm.getString("namingContext.alreadyBound", name.get(0)));
            } else {
                // Getting the type of the object and wrapping it within a new
                // NamingEntry
                Object toBind =
                        NamingManager.getStateToBind(obj, name, this, env);
                if (toBind instanceof Context) {
                    entry = new NamingEntry(name.get(0), toBind,
                            NamingEntry.CONTEXT);
                } else if (toBind instanceof LinkRef) {
                    entry = new NamingEntry(name.get(0), toBind,
                            NamingEntry.LINK_REF);
                } else if (toBind instanceof Reference) {
                    entry = new NamingEntry(name.get(0), toBind,
                            NamingEntry.REFERENCE);
                } else if (toBind instanceof Referenceable) {
                    toBind = ((Referenceable) toBind).getReference();
                    entry = new NamingEntry(name.get(0), toBind,
                            NamingEntry.REFERENCE);
                } else {
                    entry = new NamingEntry(name.get(0), toBind,
                            NamingEntry.ENTRY);
                }
                bindings.put(name.get(0), entry);
            }
        }
    }
}

