/*
 * GRAKN.AI - THE KNOWLEDGE GRAPH
 * Copyright (C) 2018 Grakn Labs Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.core.graql.executor;

import com.google.common.collect.ImmutableList;
import grakn.core.concept.thing.Entity;
import grakn.core.concept.type.EntityType;
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Role;
import grakn.core.graql.gremlin.GreedyTraversalPlan;
import grakn.core.graql.gremlin.fragment.Fragment;
import grakn.core.graql.gremlin.fragment.InIsaFragment;
import grakn.core.graql.gremlin.fragment.InSubFragment;
import grakn.core.graql.gremlin.fragment.LabelFragment;
import grakn.core.graql.gremlin.fragment.NeqFragment;
import grakn.core.graql.gremlin.fragment.OutIsaFragment;
import grakn.core.graql.gremlin.fragment.OutRolePlayerFragment;
import grakn.core.graql.gremlin.fragment.OutSubFragment;
import grakn.core.rule.GraknTestServer;
import grakn.core.server.session.TransactionOLTP;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.statement.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static graql.lang.Graql.and;
import static graql.lang.Graql.var;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

@SuppressWarnings({"CheckReturnValue", "Duplicates"})
public class IsaExplicitIT {

    @ClassRule
    public static final GraknTestServer server = new GraknTestServer();

    private TransactionOLTP tx;

    @Before
    public void loadSimpleData() {
        tx = server.sessionWithNewKeyspace().transaction().write();
        EntityType entityType0 = tx.putEntityType("entityType0");
        EntityType entityType1 = tx.putEntityType("entityType1");
        EntityType entityType2 = tx.putEntityType("entityType2");
        EntityType entityType3 = tx.putEntityType("entityType3");

        EntityType superType1 = tx.putEntityType("superType1");
        entityType0.sup(superType1);
        entityType1.sup(superType1);

        Role role1 = tx.putRole("role1");
        Role role2 = tx.putRole("role2");
        Role role3 = tx.putRole("role3");
        superType1.plays(role1).plays(role2).plays(role3);
        entityType2.plays(role1).plays(role2).plays(role3);
        entityType3.plays(role1).plays(role2).plays(role3);
        RelationType relationType = tx.putRelationType("related")
                .relates(role1).relates(role2).relates(role3);

        Entity entity1 = entityType1.create();
        Entity entity2 = entityType2.create();
        Entity entity3 = entityType3.create();
        relationType.create()
                .assign(role1, entity1)
                .assign(role2, entity2)
                .assign(role3, entity3);
    }

    @Test
    public void whenInsertIsaExplicit_InsertsADirectInstanceOfAType() {
        tx.execute(Graql.insert(var("x").isaX("superType1")));
        assertEquals(1, tx.execute(Graql.parse("match $z isa! superType1; get; count;").asGetAggregate()).get(0).number().intValue());
        assertEquals(2, tx.execute(Graql.parse("match $z isa superType1; get; count;").asGetAggregate()).get(0).number().intValue());

    }

    @Test
    public void testMatchIsaAndIsaExplicitReturnDifferentPlans() {
        Statement x = var("x");
        Statement y = var("y");

        String superType1 = "superType1";
        String entityType1 = "entityType1";
        String related = "related";

        Pattern pattern;
        ImmutableList<Fragment> plan;

        // test type without subtypes

        pattern = x.isaX(entityType1);
        plan = getPlan(pattern);
        Assert.assertEquals(2, plan.size());
        assertThat(plan, contains(
                instanceOf(LabelFragment.class),
                instanceOf(InIsaFragment.class)
        ));

        pattern = and(
                x.isaX(entityType1),
                y.isaX(entityType1),
                var().rel(x).rel(y).isa(related));
        plan = getPlan(pattern);

        Assert.assertEquals(9, plan.size());
        // 3 labels: thingy1, thingy1 and related
        assertThat(plan, contains(
                instanceOf(LabelFragment.class),
                instanceOf(LabelFragment.class),
                instanceOf(LabelFragment.class),
                instanceOf(InIsaFragment.class), // start from relation type
                instanceOf(OutRolePlayerFragment.class), // go to a role player
                instanceOf(OutIsaFragment.class), // check the role player's type
                instanceOf(OutRolePlayerFragment.class), // go to the other role player
                instanceOf(NeqFragment.class), // check two players are different
                instanceOf(OutIsaFragment.class) // check the role player's type
        ));

        // test type with subtypes

        pattern = x.isaX(superType1);
        plan = getPlan(pattern);
        Assert.assertEquals(2, plan.size());
        assertThat(plan, contains(
                instanceOf(LabelFragment.class),
                instanceOf(InIsaFragment.class)
        ));

        pattern = x.isa(superType1);
        plan = getPlan(pattern);

        Assert.assertEquals(3, plan.size());
        assertThat(plan, contains(
                instanceOf(LabelFragment.class),
                instanceOf(InSubFragment.class),
                instanceOf(InIsaFragment.class)
        ));

        pattern = and(
                x.isaX(superType1),
                y.isaX(superType1),
                var().rel(x).rel(y).isa(related));
        plan = getPlan(pattern);

        Assert.assertEquals(9, plan.size());
        // 3 labels: thingy1, thingy1 and related
        assertThat(plan, contains(
                instanceOf(LabelFragment.class),
                instanceOf(LabelFragment.class),
                instanceOf(LabelFragment.class),
                instanceOf(InIsaFragment.class), // start from relation type
                instanceOf(OutRolePlayerFragment.class), // go to a role player
                instanceOf(OutIsaFragment.class), // check the role player's type
                instanceOf(OutRolePlayerFragment.class), // go to the other role player
                instanceOf(NeqFragment.class), // check two players are different
                instanceOf(OutIsaFragment.class) // check the role player's type
        ));

        // combine isa and explicit isa

        pattern = and(
                x.isa(superType1),
                y.isaX(superType1),
                var().rel(x).rel(y).isa(related));
        plan = getPlan(pattern);

        Assert.assertEquals(10, plan.size());
        // 3 labels: thingy, thingy and related
        assertThat(plan, contains(
                instanceOf(LabelFragment.class),
                instanceOf(LabelFragment.class),
                instanceOf(LabelFragment.class),
                instanceOf(InIsaFragment.class), // start from relation type
                instanceOf(OutRolePlayerFragment.class), // go to a role player
                instanceOf(OutIsaFragment.class), // check the role player's type
                instanceOf(OutRolePlayerFragment.class), // go to the other role player
                instanceOf(NeqFragment.class), // check two players are different
                instanceOf(OutIsaFragment.class), // check the role player's type
                instanceOf(OutSubFragment.class) // check the subtypes
        ));
    }

    private ImmutableList<Fragment> getPlan(Pattern pattern) {
        return GreedyTraversalPlan.createTraversal(pattern, tx).fragments().iterator().next();
    }
}
