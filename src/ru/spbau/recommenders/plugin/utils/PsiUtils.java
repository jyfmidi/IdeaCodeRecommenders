package ru.spbau.recommenders.plugin.utils;

import com.intellij.psi.*;
import com.intellij.psi.impl.PsiSubstitutorImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Pavel Talanov
 * @author Goncharova Irina
 */
public final class PsiUtils {
    private PsiUtils() {
    }


    @Nullable
    public static String getMethodName(@NotNull PsiMethodCallExpression expression) {
        PsiReferenceExpression methodExpression = expression.getMethodExpression();
        return methodExpression.getReferenceName();
    }

    @Nullable
    public static String getReferencedName(@NotNull PsiMethodCallExpression expression) {
        PsiReferenceExpression methodExpression = expression.getMethodExpression();
        PsiExpression qualifierExpression = methodExpression.getQualifierExpression();
        if (qualifierExpression instanceof PsiReferenceExpression) {
            return ((PsiReferenceExpression) qualifierExpression).getReferenceName();
        } else {
            return null;
        }
    }

    @NotNull
    public static List<String> collectMethodCallsBeforeElementForQualifier(@NotNull final PsiElement position,
                                                                           @NotNull final PsiReferenceExpression qualifier) {
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(position, PsiMethod.class);
        //TODO:
        if (containingMethod == null) {
            return Collections.emptyList();
        }
        PsiCodeBlock body = containingMethod.getBody();
        //TODO:
        if (body == null) {
            return Collections.emptyList();
        }
        final List<String> prefixSequence = new ArrayList<String>();
        body.accept(new JavaRecursiveElementVisitor() {

            private boolean stopped = false;

            @Override
            public void visitElement(PsiElement element) {
                if (stopped) {
                    return;
                }

                super.visitElement(element);
                if (element.equals(position)) {
                    stopped = true;
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (stopped) {
                    return;
                }

                super.visitMethodCallExpression(expression);

                String methodName = getMethodName(expression);
                String referencedName = getReferencedName(expression);
                if (methodName == null || referencedName == null) {
                    return;
                }

                if (referencedName.equals(qualifier.getReferenceName())) {
                    Map<PsiTypeParameter, PsiType> map = new HashMap<PsiTypeParameter, PsiType>();
                    PsiElementFactory factory = JavaPsiFacade.getElementFactory(expression.getProject());
                    for (PsiTypeParameter parameter : expression.resolveMethod().getTypeParameters()) {
                        PsiClass superClass = parameter.getSuperClass();
                        map.put(parameter, factory.createType(superClass));
                    }
                    PsiSubstitutor substitutor = PsiSubstitutorImpl.createSubstitutor(map);
                    PsiType returnType = substitutor.substitute(expression.resolveMethod().getReturnTypeNoResolve());
                    PsiType[] types = expression.resolveMethod().getSignature(substitutor).getParameterTypes();
                    StringBuilder result = new StringBuilder();
                    result.append(methodName).append("(");
                    for (int i = 0; i < types.length; ++i) {
                        result.append(types[i].getCanonicalText());
                        if (i < types.length - 1) {
                            result.append(",");
                        }
                    }
                    result.append(")").append(returnType.getCanonicalText());
                    prefixSequence.add(result.toString());
                }
            }
        });
        return prefixSequence;
    }
}
