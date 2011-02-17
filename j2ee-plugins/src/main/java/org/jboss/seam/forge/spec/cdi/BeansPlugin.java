/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.forge.spec.cdi;

import java.io.FileNotFoundException;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.forge.parser.JavaParser;
import org.jboss.seam.forge.parser.java.JavaClass;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.constraints.RequiresFacet;
import org.jboss.seam.forge.project.facets.JavaSourceFacet;
import org.jboss.seam.forge.project.resources.builtin.java.JavaResource;
import org.jboss.seam.forge.shell.PromptType;
import org.jboss.seam.forge.shell.ShellMessages;
import org.jboss.seam.forge.shell.ShellPrompt;
import org.jboss.seam.forge.shell.events.InstallFacet;
import org.jboss.seam.forge.shell.plugins.Command;
import org.jboss.seam.forge.shell.plugins.Option;
import org.jboss.seam.forge.shell.plugins.PipeOut;
import org.jboss.seam.forge.shell.plugins.Plugin;
import org.jboss.shrinkwrap.descriptor.impl.spec.cdi.beans.Alternatives;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Named("beans")
@RequiresFacet(JavaSourceFacet.class)
public class BeansPlugin implements Plugin
{
   @Inject
   private Event<InstallFacet> install;

   @Inject
   private Project project;

   @Inject
   private ShellPrompt prompt;

   @Command("setup")
   public void setup(PipeOut out)
   {
      if (!project.hasFacet(CDIFacet.class))
      {
         install.fire(new InstallFacet(CDIFacet.class));
      }
      if (project.hasFacet(CDIFacet.class))
      {
         ShellMessages.success(out, "CDI [JSR-299] is installed.");
      }
   }

   @Command("list-interceptors")
   public void listInterceptors(PipeOut out)
   {
      CDIFacet cdi = project.getFacet(CDIFacet.class);
      List<String> interceptors = cdi.getConfig().getInterceptors();
      for (String i : interceptors)
      {
         out.println(i);
      }
   }

   @Command("list-decorators")
   public void listDecorators(PipeOut out)
   {
      CDIFacet cdi = project.getFacet(CDIFacet.class);
      List<String> decorators = cdi.getConfig().getDecorators();
      for (String d : decorators)
      {
         out.println(d);
      }
   }

   @Command("list-alternatives")
   public void listAlternatives(PipeOut out)
   {
      CDIFacet cdi = project.getFacet(CDIFacet.class);
      Alternatives alternatives = cdi.getConfig().getAlternatives();
      List<String> classes = alternatives.getClasses();
      List<String> stereotypes = alternatives.getStereotypes();
      for (int i = 0; i < classes.size() && i < stereotypes.size(); i++)
      {
         out.println(stereotypes.get(i) + " --> " + classes.get(i));
      }
   }

   @Command("list-extensions")
   public void listExtensions(PipeOut out)
   {
      CDIFacet cdi = project.getFacet(CDIFacet.class);
      List<Object> extensions = cdi.getConfig().getExtensions();
      for (Object e : extensions)
      {
         out.println(e.toString());
      }
   }

   @Command("new-bean")
   public void newBean(
            @Option(required = true,
                     name = "type") final JavaResource resource,
            @Option(required = true, name = "scoped") BeanScope scope,
            @Option(required = false, name = "overwrite") boolean overwrite
            ) throws FileNotFoundException
   {
      if (!resource.exists() || overwrite)
      {
         JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
         if (resource.createNewFile())
         {
            JavaClass javaClass = JavaParser.create(JavaClass.class);
            javaClass.setName(java.calculateName(resource));
            javaClass.setPackage(java.calculatePackage(resource));

            if (BeanScope.CUSTOM.equals(scope))
            {
               String annoType = prompt.promptCommon("Enter the qualified custom scope type:", PromptType.JAVA_CLASS);
               javaClass.addAnnotation(annoType);
            }
            else if (!BeanScope.DEPENDENT.equals(scope))
            {
               javaClass.addAnnotation(scope.getAnnotation());
            }
            resource.setContents(javaClass);
         }
      }
      else
      {
         throw new RuntimeException("Type already exists [" + resource.getFullyQualifiedName() + "]");
      }
   }
}
